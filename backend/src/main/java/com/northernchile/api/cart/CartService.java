package com.northernchile.api.cart;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.cart.dto.CartItemReq;
import com.northernchile.api.cart.dto.CartItemRes;
import com.northernchile.api.cart.dto.CartRes;
import com.northernchile.api.model.Cart;
import com.northernchile.api.model.CartItem;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.TourScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final BookingRepository bookingRepository;
    private final com.northernchile.api.availability.AvailabilityValidator availabilityValidator;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            TourScheduleRepository tourScheduleRepository,
            BookingRepository bookingRepository,
            com.northernchile.api.availability.AvailabilityValidator availabilityValidator) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        this.bookingRepository = bookingRepository;
        this.availabilityValidator = availabilityValidator;
    }

    @Transactional
    public Cart getOrCreateCart(Optional<User> currentUser, Optional<UUID> cartId) {
        if (currentUser.isPresent()) {
            // User is logged in, try to find their cart
            Cart userCart = cartRepository.findByUserId(currentUser.get().getId()).orElse(null);
            if (userCart != null) {
                // If user has a cart, potentially merge with guest cart
                if (cartId.isPresent()) {
                    mergeCarts(cartId.get(), userCart);
                }
                // Reload with details to avoid lazy loading issues
                return cartRepository.findByIdWithDetails(userCart.getId()).orElse(userCart);
            }
        }

        // Guest user or logged-in user without a cart
        if (cartId.isPresent()) {
            return cartRepository.findByIdWithDetails(cartId.get())
                    .orElseGet(this::createNewCart);
        }

        return createNewCart();
    }

    @Transactional
    public Cart addItemToCart(Cart cart, CartItemReq itemReq) {
        var schedule = tourScheduleRepository.findById(itemReq.getScheduleId())
                .orElseThrow(() -> new EntityNotFoundException("TourSchedule not found"));

        // Count participants already in THIS cart for the same schedule
        int participantsInCart = cart.getItems() != null
            ? cart.getItems().stream()
                .filter(item -> item.getSchedule().getId().equals(itemReq.getScheduleId()))
                .mapToInt(CartItem::getNumParticipants)
                .sum()
            : 0;

        // Validate availability excluding this cart (to allow adding more to same cart)
        int totalRequestedSlots = participantsInCart + itemReq.getNumParticipants();
        var availabilityResult = availabilityValidator.validateAvailability(
                schedule, totalRequestedSlots, cart.getId(), null);

        if (!availabilityResult.isAvailable()) {
            throw new IllegalStateException(availabilityResult.getErrorMessage());
        }

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setSchedule(schedule);
        newItem.setNumParticipants(itemReq.getNumParticipants());

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        cart.getItems().add(newItem);

        Cart savedCart = cartRepository.save(cart);

        // Reload cart with all relationships to avoid LazyInitializationException
        return cartRepository.findByIdWithDetails(savedCart.getId())
                .orElse(savedCart);
    }

    @Transactional
    public Cart removeItemFromCart(Cart cart, UUID itemId) {
        cartItemRepository.deleteById(itemId);
        // Refresh cart from DB to reflect removed item with all relationships loaded
        return cartRepository.findByIdWithDetails(cart.getId()).orElseThrow();
    }

    private Cart createNewCart() {
        Cart cart = new Cart();
        cart.setStatus("ACTIVE");
        cart.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS)); // Cart expires in 30 days
        return cartRepository.save(cart);
    }

    private void mergeCarts(UUID guestCartId, Cart userCart) {
        cartRepository.findById(guestCartId).ifPresent(guestCart -> {
            if (!guestCart.getId().equals(userCart.getId())) {
                guestCart.getItems().forEach(guestItem -> {
                    // Check if user already has an item for this schedule
                    CartItem existingItem = userCart.getItems().stream()
                            .filter(item -> item.getSchedule().getId().equals(guestItem.getSchedule().getId()))
                            .findFirst()
                            .orElse(null);

                    if (existingItem != null) {
                        // Merge: Add guest item participants to existing item
                        existingItem.setNumParticipants(existingItem.getNumParticipants() + guestItem.getNumParticipants());
                        // Note: CartItem doesn't store itemTotal, it's calculated on-the-fly from schedule.tour.price * numParticipants
                        cartItemRepository.save(existingItem);
                        // Guest item will be deleted with the guest cart
                    } else {
                        // No duplicate: Move item to user cart
                        guestItem.setCart(userCart);
                        userCart.getItems().add(guestItem);
                    }
                });
                cartRepository.save(userCart);
                cartRepository.delete(guestCart);
            }
        });
    }

    @Transactional(readOnly = true)
    public CartRes toCartRes(Cart cart) {
        CartRes res = new CartRes();
        res.setCartId(cart.getId());

        List<CartItemRes> itemResponses = cart.getItems() == null ? new ArrayList<>() : cart.getItems().stream().map(item -> {
            CartItemRes itemRes = new CartItemRes();
            itemRes.setItemId(item.getId());
            itemRes.setScheduleId(item.getSchedule().getId());
            itemRes.setTourId(item.getSchedule().getTour().getId());
            itemRes.setTourName(item.getSchedule().getTour().getNameTranslations().get("es"));
            itemRes.setNumParticipants(item.getNumParticipants());

            BigDecimal pricePerParticipant = item.getSchedule().getTour().getPrice();
            itemRes.setPricePerParticipant(pricePerParticipant);

            BigDecimal itemTotal = pricePerParticipant.multiply(BigDecimal.valueOf(item.getNumParticipants()));
            itemRes.setItemTotal(itemTotal);
            return itemRes;
        }).collect(Collectors.toList());

        res.setItems(itemResponses);

        BigDecimal cartTotal = itemResponses.stream()
                .map(CartItemRes::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        res.setCartTotal(cartTotal);

        return res;
    }

    /**
     * Scheduled task to delete expired carts
     * Runs every hour to cleanup carts that have passed their expiration time
     */
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredCarts() {
        Instant now = Instant.now();
        int deletedCount = cartRepository.deleteByExpiresAtBefore(now);
        if (deletedCount > 0) {
            org.slf4j.LoggerFactory.getLogger(CartService.class)
                .info("Cleaned up {} expired carts", deletedCount);
        }
    }
}
