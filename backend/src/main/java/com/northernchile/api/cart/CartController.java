package com.northernchile.api.cart;

import com.northernchile.api.model.Cart;
import com.northernchile.api.model.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart(@CookieValue(name = "cartId", required = false) String cartId) {
        Cart cart = cartService.getOrCreateCart(cartId);
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addItemToCart(
            @CookieValue(name = "cartId", required = false) String cartId,
            @RequestBody CartItem item) {
        Cart updatedCart = cartService.addItemToCart(cartId, item);
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Cart> removeItemFromCart(
            @CookieValue(name = "cartId", required = false) String cartId,
            @PathVariable UUID itemId) {
        Cart updatedCart = cartService.removeItemFromCart(cartId, itemId);
        return new ResponseEntity<>(updatedCart, HttpStatus.OK);
    }
}
