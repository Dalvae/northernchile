package com.northernchile.api.cart.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CartItemReq {
    private UUID tourId;
    private LocalDate tourDate;
    private int numAdults;
    private int numChildren;
}
