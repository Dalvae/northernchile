package com.northernchile.api.pricing;

import com.northernchile.api.config.properties.AppProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Centralized pricing service for consistent price calculations.
 *
 * IMPORTANT: Tour prices in the database are TAX-INCLUSIVE (IVA incluido).
 * This means the displayed price IS the final price customers pay.
 *
 * When calculating subtotal and tax:
 * - totalAmount = pricePerParticipant × participantCount (this is what the customer pays)
 * - subtotal = totalAmount / (1 + taxRate) (back-calculated net price)
 * - taxAmount = totalAmount - subtotal (the tax portion)
 *
 * This ensures:
 * 1. The price shown to customers is exactly what they pay
 * 2. Tax calculations are consistent across cart, booking, and payment
 * 3. No rounding discrepancies between frontend and backend
 */
@Service
public class PricingService {

    private final AppProperties appProperties;

    public PricingService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * Calculate pricing breakdown for a given total amount (tax-inclusive).
     * Use this when you have the final price and need to extract the tax component.
     */
    public PricingResult calculateFromTaxInclusiveAmount(BigDecimal totalAmount) {
        // Back-calculate subtotal from tax-inclusive total
        // subtotal = total / (1 + taxRate)
        BigDecimal taxRate = BigDecimal.valueOf(appProperties.getTaxRate());
        BigDecimal subtotal = totalAmount.divide(
            BigDecimal.ONE.add(taxRate),
            0, // CLP has no decimals
            RoundingMode.HALF_UP
        );
        BigDecimal taxAmount = totalAmount.subtract(subtotal);

        return new PricingResult(subtotal, taxAmount, totalAmount, taxRate);
    }

    /**
     * Calculate pricing for a line item (price per participant × count).
     * Prices are assumed to be tax-inclusive.
     */
    public PricingResult calculateLineItem(BigDecimal pricePerParticipant, int participantCount) {
        BigDecimal totalAmount = pricePerParticipant.multiply(BigDecimal.valueOf(participantCount));
        return calculateFromTaxInclusiveAmount(totalAmount);
    }

    /**
     * Calculate pricing for multiple items.
     * Each item's price is assumed to be tax-inclusive.
     */
    public PricingResult calculateMultipleItems(java.util.List<LineItem> items) {
        BigDecimal totalAmount = items.stream()
            .map(item -> item.pricePerUnit().multiply(BigDecimal.valueOf(item.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return calculateFromTaxInclusiveAmount(totalAmount);
    }

    /**
     * Get the current tax rate.
     */
    public BigDecimal getTaxRate() {
        return BigDecimal.valueOf(appProperties.getTaxRate());
    }

    /**
     * Result of a pricing calculation.
     */
    public record PricingResult(
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        BigDecimal taxRate
    ) {}

    /**
     * Represents a line item for pricing calculation.
     */
    public record LineItem(
        BigDecimal pricePerUnit,
        int quantity
    ) {}
}
