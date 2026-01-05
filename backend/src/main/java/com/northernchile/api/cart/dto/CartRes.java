package com.northernchile.api.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID cartId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<CartItemRes> items,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal cartTotal,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal subtotal,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal taxAmount,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal taxRate
) {}
