package com.northernchile.api.cart.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CartRes {
    private UUID cartId;
    private List<CartItemRes> items;
    private BigDecimal cartTotal;
}
