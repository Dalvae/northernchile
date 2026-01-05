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

        for (var item : request.items()) {
            var schedule = tourScheduleRepository.findById(item.scheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("TourSchedule", item.scheduleId()));

            var tour = schedule.getTour();
            BigDecimal pricePerParticipant = tour.getPrice();
            BigDecimal lineTotal = pricePerParticipant.multiply(BigDecimal.valueOf(item.numParticipants()));

            var lineItem = new PricingCalculationRes.PricingLineItem(
                schedule.getId(),
                tour.getId(),
                tour.getNameTranslations().get("es"),
                item.numParticipants(),
                pricePerParticipant,
                lineTotal
            );
            lineItems.add(lineItem);

            pricingItems.add(new PricingService.LineItem(pricePerParticipant, item.numParticipants()));
        }

        var pricing = pricingService.calculateMultipleItems(pricingItems);

        var response = new PricingCalculationRes(
            lineItems,
            pricing.subtotal(),
            pricing.taxAmount(),
            pricing.taxRate(),
            pricing.totalAmount()
        );

        return ResponseEntity.ok(response);
    }
}
