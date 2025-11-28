package com.northernchile.api.exception;

import java.util.UUID;

/**
 * Exception thrown when attempting to use an expired cart.
 * Returns HTTP 410 Gone.
 */
public class CartExpiredException extends BusinessException {

    public static final String ERROR_CODE = "CART_EXPIRED";

    private final UUID cartId;

    public CartExpiredException(UUID cartId) {
        super(ERROR_CODE, String.format("Cart %s has expired. Please create a new cart.", cartId));
        this.cartId = cartId;
    }

    public UUID getCartId() {
        return cartId;
    }
}
