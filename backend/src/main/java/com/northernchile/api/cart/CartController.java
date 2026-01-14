package com.northernchile.api.cart;

import com.northernchile.api.cart.dto.CartItemReq;
import com.northernchile.api.cart.dto.CartRes;
import com.northernchile.api.model.Cart;
import com.northernchile.api.model.User;
import com.northernchile.api.security.AuthorizationService;
import com.northernchile.api.util.CookieHelper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final AuthorizationService authorizationService;
    private final CookieHelper cookieHelper;

    public CartController(CartService cartService, AuthorizationService authorizationService, CookieHelper cookieHelper) {
        this.cartService = cartService;
        this.authorizationService = authorizationService;
        this.cookieHelper = cookieHelper;
    }

    @GetMapping
    public ResponseEntity<CartRes> getCart(@CookieValue(name = "cartId", required = false) String cartId) {
        Cart cart = cartService.getOrCreateCart(getCurrentUser(), getCartId(cartId));
        return ResponseEntity.ok(cartService.toCartRes(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<CartRes> addItemToCart(
            @CookieValue(name = "cartId", required = false) String cartId,
            @jakarta.validation.Valid @RequestBody CartItemReq itemReq,
            HttpServletResponse response) {
        Cart cart = cartService.getOrCreateCart(getCurrentUser(), getCartId(cartId));
        Cart updatedCart = cartService.addItemToCart(cart, itemReq);
        cookieHelper.setCartIdCookie(response, updatedCart.getId().toString());
        return ResponseEntity.ok(cartService.toCartRes(updatedCart));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartRes> removeItemFromCart(
            @CookieValue(name = "cartId", required = false) String cartId,
            @PathVariable UUID itemId) {
        Cart cart = cartService.getOrCreateCart(getCurrentUser(), getCartId(cartId));
        Cart updatedCart = cartService.removeItemFromCart(cart, itemId);
        return ResponseEntity.ok(cartService.toCartRes(updatedCart));
    }

    /**
     * Clear the entire cart (used after successful payment).
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @CookieValue(name = "cartId", required = false) String cartId,
            HttpServletResponse response) {
        cartService.clearCart(getCurrentUser(), getCartId(cartId));
        cookieHelper.clearCartIdCookie(response);
        return ResponseEntity.noContent().build();
    }

    private Optional<User> getCurrentUser() {
        return Optional.ofNullable(authorizationService.getCurrentUser());
    }

    private Optional<UUID> getCartId(String cartId) {
        try {
            return Optional.of(UUID.fromString(cartId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
