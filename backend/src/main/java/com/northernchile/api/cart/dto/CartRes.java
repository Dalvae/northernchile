
package com.northernchile.api.cart.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CartRes {
    private UUID cartId;
    private List<CartItemRes> items;
    private BigDecimal cartTotal;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal taxRate;

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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartRes cartRes = (CartRes) o;
        return Objects.equals(cartId, cartRes.cartId) && Objects.equals(items, cartRes.items) && Objects.equals(cartTotal, cartRes.cartTotal) && Objects.equals(subtotal, cartRes.subtotal) && Objects.equals(taxAmount, cartRes.taxAmount) && Objects.equals(taxRate, cartRes.taxRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cartId, items, cartTotal, subtotal, taxAmount, taxRate);
    }

    @Override
    public String toString() {
        return "CartRes{" +
                "cartId=" + cartId +
                ", items=" + items +
                ", subtotal=" + subtotal +
                ", taxAmount=" + taxAmount +
                ", cartTotal=" + cartTotal +
                ", taxRate=" + taxRate +
                '}';
    }
}
