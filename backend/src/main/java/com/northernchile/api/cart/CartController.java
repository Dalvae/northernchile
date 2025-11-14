package com.northernchile.api.cart;

import com.northernchile.api.cart.dto.CartItemReq;
import com.northernchile.api.cart.dto.CartRes;
import com.northernchile.api.model.Cart;
import com.northernchile.api.model.User;
import com.northernchile.api.user.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
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
        setCartIdCookie(response, updatedCart.getId());
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

    private Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        String currentPrincipalName = authentication.getName();
        return userRepository.findByEmail(currentPrincipalName);
    }

    private Optional<UUID> getCartId(String cartId) {
        try {
            return Optional.of(UUID.fromString(cartId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void setCartIdCookie(HttpServletResponse response, UUID cartId) {
        Cookie cookie = new Cookie("cartId", cartId.toString());
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS only
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
        cookie.setAttribute("SameSite", "Lax"); // CSRF protection
        response.addCookie(cookie);
    }
}
