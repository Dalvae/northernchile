package com.northernchile.api.cart;

import com.northernchile.api.cart.dto.CartItemReq;
import com.northernchile.api.cart.dto.CartItemRes;
import com.northernchile.api.cart.dto.CartRes;
import com.northernchile.api.model.Cart;
import com.northernchile.api.model.CartItem;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.TourRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
    private TourRepository tourRepository;

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
        var tour = tourRepository.findById(itemReq.getTourId())
                .orElseThrow(() -> new EntityNotFoundException("Tour not found"));

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setTour(tour);
        newItem.setTourDate(itemReq.getTourDate());
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
            itemRes.setTourId(item.getTour().getId());
            itemRes.setTourName(item.getTour().getName());
            itemRes.setTourDate(item.getTourDate());
            itemRes.setNumAdults(item.getNumAdults());
            itemRes.setNumChildren(item.getNumChildren());
            itemRes.setPriceAdult(item.getTour().getPriceAdult());
            itemRes.setPriceChild(item.getTour().getPriceChild());

            BigDecimal adultTotal = item.getTour().getPriceAdult().multiply(BigDecimal.valueOf(item.getNumAdults()));
            BigDecimal childTotal = item.getTour().getPriceChild() != null ?
                    item.getTour().getPriceChild().multiply(BigDecimal.valueOf(item.getNumChildren())) : BigDecimal.ZERO;
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
