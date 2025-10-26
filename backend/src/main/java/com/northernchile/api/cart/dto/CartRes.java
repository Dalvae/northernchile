
package com.northernchile.api.cart.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CartRes {
    private UUID cartId;
    private List<CartItemRes> items;
    private BigDecimal cartTotal;

    public CartRes() {
    }

    public CartRes(UUID cartId, List<CartItemRes> items, BigDecimal cartTotal) {
        this.cartId = cartId;
        this.items = items;
        this.cartTotal = cartTotal;
    }

    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID cartId) {
        this.cartId = cartId;
    }

    public List<CartItemRes> getItems() {
        return items;
    }

    public void setItems(List<CartItemRes> items) {
        this.items = items;
    }

    public BigDecimal getCartTotal() {
        return cartTotal;
    }

    public void setCartTotal(BigDecimal cartTotal) {
        this.cartTotal = cartTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartRes cartRes = (CartRes) o;
        return Objects.equals(cartId, cartRes.cartId) && Objects.equals(items, cart.items) && Objects.equals(cartTotal, cartRes.cartTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartId, items, cartTotal);
    }

    @Override
    public String toString() {
        return "CartRes{" +
                "cartId=" + cartId +
                ", items=" + items +
                ", cartTotal=" + cartTotal +
                '}';
    }
}
