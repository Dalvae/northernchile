package com.northernchile.api.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

public record FinancialReport(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant periodStart,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant periodEnd,
    // Mercado Pago - datos reales de la API
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal mpGrossAmount,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal mpGatewayFees,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal mpNetReceived,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long mpTransactionCount,
    // Transbank - datos estimados (reconciliar con extracto)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal tbGrossAmount,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal tbEstimatedFees,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal tbEstimatedNet,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long tbTransactionCount,
    // Totales
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal totalGrossAmount,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal totalFees,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal totalNetReceived,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long totalTransactionCount,
    // IVA a pagar (19% del neto - ya calculado en booking.taxAmount)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal totalTaxCollected,
    // Ingreso neto real (después de comisiones e IVA)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal netRevenue
) {
    /**
     * Factory method that calculates totals automatically.
     */
    public static FinancialReport create(
            Instant periodStart, Instant periodEnd,
            BigDecimal mpGrossAmount, BigDecimal mpGatewayFees, BigDecimal mpNetReceived, long mpTransactionCount,
            BigDecimal tbGrossAmount, BigDecimal tbEstimatedFees, BigDecimal tbEstimatedNet, long tbTransactionCount,
            BigDecimal totalTaxCollected) {
        BigDecimal totalGrossAmount = mpGrossAmount.add(tbGrossAmount);
        BigDecimal totalFees = mpGatewayFees.add(tbEstimatedFees);
        BigDecimal totalNetReceived = mpNetReceived.add(tbEstimatedNet);
        long totalTransactionCount = mpTransactionCount + tbTransactionCount;
        BigDecimal netRevenue = totalNetReceived.subtract(totalTaxCollected);

        return new FinancialReport(
            periodStart, periodEnd,
            mpGrossAmount, mpGatewayFees, mpNetReceived, mpTransactionCount,
            tbGrossAmount, tbEstimatedFees, tbEstimatedNet, tbTransactionCount,
            totalGrossAmount, totalFees, totalNetReceived, totalTransactionCount,
            totalTaxCollected, netRevenue
        );
    }
}
