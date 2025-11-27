package com.northernchile.api.reports.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class FinancialReport {

    private Instant periodStart;
    private Instant periodEnd;

    // Mercado Pago - datos reales de la API
    private BigDecimal mpGrossAmount;
    private BigDecimal mpGatewayFees;
    private BigDecimal mpNetReceived;
    private long mpTransactionCount;

    // Transbank - datos estimados (reconciliar con extracto)
    private BigDecimal tbGrossAmount;
    private BigDecimal tbEstimatedFees;
    private BigDecimal tbEstimatedNet;
    private long tbTransactionCount;

    // Totales
    private BigDecimal totalGrossAmount;
    private BigDecimal totalFees;
    private BigDecimal totalNetReceived;
    private long totalTransactionCount;

    // IVA a pagar (19% del neto - ya calculado en booking.taxAmount)
    private BigDecimal totalTaxCollected;

    // Ingreso neto real (después de comisiones e IVA)
    private BigDecimal netRevenue;

    public FinancialReport() {
    }

    public FinancialReport(
            Instant periodStart,
            Instant periodEnd,
            BigDecimal mpGrossAmount,
            BigDecimal mpGatewayFees,
            BigDecimal mpNetReceived,
            long mpTransactionCount,
            BigDecimal tbGrossAmount,
            BigDecimal tbEstimatedFees,
            BigDecimal tbEstimatedNet,
            long tbTransactionCount,
            BigDecimal totalTaxCollected) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.mpGrossAmount = mpGrossAmount;
        this.mpGatewayFees = mpGatewayFees;
        this.mpNetReceived = mpNetReceived;
        this.mpTransactionCount = mpTransactionCount;
        this.tbGrossAmount = tbGrossAmount;
        this.tbEstimatedFees = tbEstimatedFees;
        this.tbEstimatedNet = tbEstimatedNet;
        this.tbTransactionCount = tbTransactionCount;
        this.totalTaxCollected = totalTaxCollected;

        // Calculate totals
        this.totalGrossAmount = mpGrossAmount.add(tbGrossAmount);
        this.totalFees = mpGatewayFees.add(tbEstimatedFees);
        this.totalNetReceived = mpNetReceived.add(tbEstimatedNet);
        this.totalTransactionCount = mpTransactionCount + tbTransactionCount;

        // Net revenue = what we receive - taxes we owe
        this.netRevenue = this.totalNetReceived.subtract(totalTaxCollected);
    }

    public Instant getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Instant periodStart) {
        this.periodStart = periodStart;
    }

    public Instant getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Instant periodEnd) {
        this.periodEnd = periodEnd;
    }

    public BigDecimal getMpGrossAmount() {
        return mpGrossAmount;
    }

    public void setMpGrossAmount(BigDecimal mpGrossAmount) {
        this.mpGrossAmount = mpGrossAmount;
    }

    public BigDecimal getMpGatewayFees() {
        return mpGatewayFees;
    }

    public void setMpGatewayFees(BigDecimal mpGatewayFees) {
        this.mpGatewayFees = mpGatewayFees;
    }

    public BigDecimal getMpNetReceived() {
        return mpNetReceived;
    }

    public void setMpNetReceived(BigDecimal mpNetReceived) {
        this.mpNetReceived = mpNetReceived;
    }

    public long getMpTransactionCount() {
        return mpTransactionCount;
    }

    public void setMpTransactionCount(long mpTransactionCount) {
        this.mpTransactionCount = mpTransactionCount;
    }

    public BigDecimal getTbGrossAmount() {
        return tbGrossAmount;
    }

    public void setTbGrossAmount(BigDecimal tbGrossAmount) {
        this.tbGrossAmount = tbGrossAmount;
    }

    public BigDecimal getTbEstimatedFees() {
        return tbEstimatedFees;
    }

    public void setTbEstimatedFees(BigDecimal tbEstimatedFees) {
        this.tbEstimatedFees = tbEstimatedFees;
    }

    public BigDecimal getTbEstimatedNet() {
        return tbEstimatedNet;
    }

    public void setTbEstimatedNet(BigDecimal tbEstimatedNet) {
        this.tbEstimatedNet = tbEstimatedNet;
    }

    public long getTbTransactionCount() {
        return tbTransactionCount;
    }

    public void setTbTransactionCount(long tbTransactionCount) {
        this.tbTransactionCount = tbTransactionCount;
    }

    public BigDecimal getTotalGrossAmount() {
        return totalGrossAmount;
    }

    public void setTotalGrossAmount(BigDecimal totalGrossAmount) {
        this.totalGrossAmount = totalGrossAmount;
    }

    public BigDecimal getTotalFees() {
        return totalFees;
    }

    public void setTotalFees(BigDecimal totalFees) {
        this.totalFees = totalFees;
    }

    public BigDecimal getTotalNetReceived() {
        return totalNetReceived;
    }

    public void setTotalNetReceived(BigDecimal totalNetReceived) {
        this.totalNetReceived = totalNetReceived;
    }

    public long getTotalTransactionCount() {
        return totalTransactionCount;
    }

    public void setTotalTransactionCount(long totalTransactionCount) {
        this.totalTransactionCount = totalTransactionCount;
    }

    public BigDecimal getTotalTaxCollected() {
        return totalTaxCollected;
    }

    public void setTotalTaxCollected(BigDecimal totalTaxCollected) {
        this.totalTaxCollected = totalTaxCollected;
    }

    public BigDecimal getNetRevenue() {
        return netRevenue;
    }

    public void setNetRevenue(BigDecimal netRevenue) {
        this.netRevenue = netRevenue;
    }
}
