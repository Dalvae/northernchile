package com.northernchile.api.pricing;

import com.northernchile.api.exception.ResourceNotFoundException;
import com.northernchile.api.pricing.dto.PricingCalculationReq;
import com.northernchile.api.pricing.dto.PricingCalculationRes;
import com.northernchile.api.tour.TourScheduleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for pricing calculations.
 * Provides a "single source of truth" for price calculations to avoid
 * discrepancies between frontend and backend.
 */
@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingService pricingService;
    private final TourScheduleRepository tourScheduleRepository;

    public PricingController(PricingService pricingService, TourScheduleRepository tourScheduleRepository) {
        this.pricingService = pricingService;
        this.tourScheduleRepository = tourScheduleRepository;
    }

    /**
     * Calculate pricing for a set of items.
     * This endpoint is the authoritative source for all price calculations.
     * Frontend should use this before checkout to ensure price consistency.
     */
    @PostMapping("/calculate")
    public ResponseEntity<PricingCalculationRes> calculatePricing(@RequestBody PricingCalculationReq request) {
        List<PricingCalculationRes.PricingLineItem> lineItems = new ArrayList<>();
        List<PricingService.LineItem> pricingItems = new ArrayList<>();

        for (var item : request.getItems()) {
            var schedule = tourScheduleRepository.findById(item.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("TourSchedule", item.getScheduleId()));

            var tour = schedule.getTour();
            BigDecimal pricePerParticipant = tour.getPrice();
            BigDecimal lineTotal = pricePerParticipant.multiply(BigDecimal.valueOf(item.getNumParticipants()));

            var lineItem = new PricingCalculationRes.PricingLineItem();
            lineItem.setScheduleId(schedule.getId());
            lineItem.setTourId(tour.getId());
            lineItem.setTourName(tour.getNameTranslations().get("es"));
            lineItem.setNumParticipants(item.getNumParticipants());
            lineItem.setPricePerParticipant(pricePerParticipant);
            lineItem.setLineTotal(lineTotal);
            lineItems.add(lineItem);

            pricingItems.add(new PricingService.LineItem(pricePerParticipant, item.getNumParticipants()));
        }

        var pricing = pricingService.calculateMultipleItems(pricingItems);

        var response = new PricingCalculationRes();
        response.setItems(lineItems);
        response.setSubtotal(pricing.subtotal());
        response.setTaxAmount(pricing.taxAmount());
        response.setTaxRate(pricing.taxRate());
        response.setTotalAmount(pricing.totalAmount());

        return ResponseEntity.ok(response);
    }
}
