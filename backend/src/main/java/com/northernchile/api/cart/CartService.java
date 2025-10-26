package com.northernchile.api.cart;

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
public class CartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private TourScheduleRepository tourScheduleRepository;

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
                return userCart;
            }
        }

        // Guest user or logged-in user without a cart
        if (cartId.isPresent()) {
            return cartRepository.findById(cartId.get()).orElseGet(this::createNewCart);
        }

        return createNewCart();
    }

    @Transactional
    public Cart addItemToCart(Cart cart, CartItemReq itemReq) {
        var schedule = tourScheduleRepository.findById(itemReq.getScheduleId())
                .orElseThrow(() -> new EntityNotFoundException("TourSchedule not found"));

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setSchedule(schedule);
        newItem.setNumAdults(itemReq.getNumAdults());
        newItem.setNumChildren(itemReq.getNumChildren());

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        cart.getItems().add(newItem);

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItemFromCart(Cart cart, UUID itemId) {
        cartItemRepository.deleteById(itemId);
        // Refresh cart from DB to reflect removed item
        return cartRepository.findById(cart.getId()).orElseThrow();
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
                guestCart.getItems().forEach(item -> {
                    item.setCart(userCart);
                    userCart.getItems().add(item);
                });
                cartRepository.save(userCart);
                cartRepository.delete(guestCart);
            }
        });
    }

    public CartRes toCartRes(Cart cart) {
        CartRes res = new CartRes();
        res.setCartId(cart.getId());

        List<CartItemRes> itemResponses = cart.getItems() == null ? new ArrayList<>() : cart.getItems().stream().map(item -> {
            CartItemRes itemRes = new CartItemRes();
            itemRes.setItemId(item.getId());
            itemRes.setScheduleId(item.getSchedule().getId());
            itemRes.setTourId(item.getSchedule().getTour().getId());
            itemRes.setTourName(item.getSchedule().getTour().getName());
            itemRes.setNumAdults(item.getNumAdults());
            itemRes.setNumChildren(item.getNumChildren());
            itemRes.setPriceAdult(item.getSchedule().getTour().getPriceAdult());
            itemRes.setPriceChild(item.getSchedule().getTour().getPriceChild());

            BigDecimal adultTotal = item.getSchedule().getTour().getPriceAdult().multiply(BigDecimal.valueOf(item.getNumAdults()));
            BigDecimal childTotal = item.getSchedule().getTour().getPriceChild() != null ?
                    item.getSchedule().getTour().getPriceChild().multiply(BigDecimal.valueOf(item.getNumChildren())) : BigDecimal.ZERO;
            itemRes.setItemTotal(adultTotal.add(childTotal));
            return itemRes;
        }).collect(Collectors.toList());

        res.setItems(itemResponses);

        BigDecimal cartTotal = itemResponses.stream()
                .map(CartItemRes::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        res.setCartTotal(cartTotal);

        return res;
    }
}
