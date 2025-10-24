package com.northernchile.api.cart.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CartItemRes {
    private UUID itemId;
    private UUID tourId;
    private String tourName;
    private LocalDate tourDate;
    private int numAdults;
    private int numChildren;
    private BigDecimal priceAdult;
    private BigDecimal priceChild;
    private BigDecimal itemTotal;
}
