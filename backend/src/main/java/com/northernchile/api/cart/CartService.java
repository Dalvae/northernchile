package com.northernchile.api.cart;

import com.northernchile.api.cart.dto.CartItemReq;
import com.northernchile.api.cart.dto.CartItemRes;
import com.northernchile.api.cart.dto.CartRes;
import com.northernchile.api.exception.ResourceNotFoundException;
import com.northernchile.api.exception.ScheduleFullException;
import com.northernchile.api.model.Cart;
import com.northernchile.api.model.CartItem;
import com.northernchile.api.model.User;
import com.northernchile.api.pricing.PricingService;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.tour.TourUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final com.northernchile.api.availability.AvailabilityValidator availabilityValidator;
    private final PricingService pricingService;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            TourScheduleRepository tourScheduleRepository,
            com.northernchile.api.availability.AvailabilityValidator availabilityValidator,
            PricingService pricingService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        this.availabilityValidator = availabilityValidator;
        this.pricingService = pricingService;
    }

    @Transactional
    public Cart getOrCreateCart(Optional<User> currentUser, Optional<UUID> cartId) {
        if (currentUser.isPresent()) {
            User user = currentUser.get();
            // User is logged in, try to find their cart
            Cart userCart = cartRepository.findByUserId(user.getId()).orElse(null);
            
            if (userCart != null) {
                // User already has a cart, merge guest cart if present
                if (cartId.isPresent()) {
                    mergeCarts(cartId.get(), userCart);
                }
                return cartRepository.findByIdWithDetails(userCart.getId()).orElse(userCart);
            }
            
            // User doesn't have a cart yet
            if (cartId.isPresent()) {
                // Adopt the guest cart (associate it with the user)
                Cart guestCart = cartRepository.findByIdWithDetails(cartId.get()).orElse(null);
                if (guestCart != null && guestCart.getUser() == null) {
                    guestCart.setUser(user);
                    guestCart.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
                    return cartRepository.save(guestCart);
                }
            }
            
            // No guest cart to adopt, create a new one for the user
            return createNewCart(user);
        }

        // Guest user (not logged in)
        if (cartId.isPresent()) {
            return cartRepository.findByIdWithDetails(cartId.get())
                    .orElseGet(() -> createNewCart(null));
        }

        return createNewCart(null);
    }

    @Transactional
    public Cart addItemToCart(Cart cart, CartItemReq itemReq) {
        var schedule = tourScheduleRepository.findById(itemReq.scheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("TourSchedule", itemReq.scheduleId()));

        // Count participants already in THIS cart for the same schedule
        int participantsInCart = cart.getItems() != null
            ? cart.getItems().stream()
                .filter(item -> item.getSchedule().getId().equals(itemReq.scheduleId()))
                .mapToInt(CartItem::getNumParticipants)
                .sum()
            : 0;

        // Validate availability excluding this cart (to allow adding more to same cart)
        int totalRequestedSlots = participantsInCart + itemReq.numParticipants();
        var availabilityResult = availabilityValidator.validateAvailability(
                schedule, totalRequestedSlots, cart.getId(), null);

        if (!availabilityResult.available()) {
            throw new ScheduleFullException(availabilityResult.errorMessage());
        }

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        // Check if there's already an item for this schedule - consolidate instead of creating new
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getSchedule().getId().equals(itemReq.scheduleId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update existing item instead of creating a new one
            existingItem.setNumParticipants(existingItem.getNumParticipants() + itemReq.numParticipants());
        } else {
            // Create new item only if no existing item for this schedule
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setSchedule(schedule);
            newItem.setNumParticipants(itemReq.numParticipants());
            cart.getItems().add(newItem);
        }

        // Extend cart expiration when items are added
        cart.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));

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

    private Cart createNewCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setStatus("ACTIVE");
        cart.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS)); // Cart expires in 1 hour
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
        // Build line items and collect pricing data
        List<CartItemRes> itemResponses = new ArrayList<>();
        List<PricingService.LineItem> pricingItems = new ArrayList<>();

        if (cart.getItems() != null) {
            for (var item : cart.getItems()) {
                BigDecimal pricePerParticipant = item.getSchedule().getTour().getPrice();
                BigDecimal itemTotal = pricePerParticipant.multiply(BigDecimal.valueOf(item.getNumParticipants()));

                CartItemRes itemRes = new CartItemRes(
                        item.getId(),
                        item.getSchedule().getId(),
                        item.getSchedule().getTour().getId(),
                        TourUtils.getTourName(item.getSchedule().getTour()),
                        item.getNumParticipants(),
                        pricePerParticipant,
                        itemTotal,
                        item.getSchedule().getTour().getDurationHours(),
                        item.getSchedule().getStartDatetime()
                );
                itemResponses.add(itemRes);

                pricingItems.add(new PricingService.LineItem(pricePerParticipant, item.getNumParticipants()));
            }
        }

        // Use centralized pricing service for consistent tax calculations
        var pricing = pricingService.calculateMultipleItems(pricingItems);

        return new CartRes(
                cart.getId(),
                itemResponses,
                pricing.totalAmount(),
                pricing.subtotal(),
                pricing.taxAmount(),
                pricing.taxRate()
        );
    }

    /**
     * Clear (delete) the user's cart completely.
     * Called after successful payment to remove all items.
     */
    @Transactional
    public void clearCart(Optional<User> user, Optional<UUID> cartId) {
        Optional<Cart> cartOpt = Optional.empty();
        
        // Try to find cart by user ID first
        if (user.isPresent()) {
            cartOpt = cartRepository.findByUserId(user.get().getId());
        }
        
        // Fall back to cartId cookie
        if (cartOpt.isEmpty() && cartId.isPresent()) {
            cartOpt = cartRepository.findById(cartId.get());
        }
        
        // Delete the cart if found
        cartOpt.ifPresent(cartRepository::delete);
    }

    /**
     * Scheduled task to delete expired carts.
     * Runs every 15 minutes to cleanup carts that have passed their expiration time.
     * Interval increased from 5 min to reduce database compute costs on Neon.
     */
    @org.springframework.scheduling.annotation.Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void cleanupExpiredCarts() {
        Instant now = Instant.now();
        // Quick check to avoid unnecessary DELETE when no expired carts exist
        if (!cartRepository.existsExpiredCarts(now)) {
            return;
        }
        int deletedCount = cartRepository.deleteByExpiresAtBefore(now);
        if (deletedCount > 0) {
            org.slf4j.LoggerFactory.getLogger(CartService.class)
                .info("Cleaned up {} expired carts", deletedCount);
        }
    }
}
