package com.northernchile.api.reports;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.config.properties.PaymentProperties;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.BookingStatus;
import com.northernchile.api.payment.model.Payment;
import com.northernchile.api.payment.model.PaymentProvider;
import com.northernchile.api.payment.repository.PaymentRepository;
import com.northernchile.api.reports.dto.BookingsByDayReport;
import com.northernchile.api.reports.dto.FinancialReport;
import com.northernchile.api.reports.dto.OverviewReport;
import com.northernchile.api.reports.dto.TopTourReport;
import com.northernchile.api.tour.TourRepository;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.tour.TourUtils;
import com.northernchile.api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.northernchile.api.util.DateTimeUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportsService {

    private static final Logger log = LoggerFactory.getLogger(ReportsService.class);
    private static final int DEFAULT_PERIOD_DAYS = 30;

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TourRepository tourRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final UserRepository userRepository;
    private final PaymentProperties paymentProperties;

    public ReportsService(
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            TourRepository tourRepository,
            TourScheduleRepository tourScheduleRepository,
            UserRepository userRepository,
            PaymentProperties paymentProperties) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.tourRepository = tourRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        this.userRepository = userRepository;
        this.paymentProperties = paymentProperties;
    }

    public OverviewReport getOverview(Instant start, Instant end) {
        // Use query with JOIN FETCH for participants to avoid N+1 in countTotalParticipants
        List<Booking> bookings = bookingRepository.findByCreatedAtBetweenWithTourInfo(start, end);

        long totalBookings = bookings.size();
        long confirmedBookings = countConfirmedBookings(bookings);
        long cancelledBookings = countCancelledBookings(bookings);
        BigDecimal totalRevenue = calculateTotalRevenue(bookings);
        long totalParticipants = countTotalParticipants(bookings);

        BigDecimal averageBookingValue = confirmedBookings > 0
                ? totalRevenue.divide(BigDecimal.valueOf(confirmedBookings), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        double conversionRate = totalBookings > 0
                ? (confirmedBookings * 100.0 / totalBookings)
                : 0.0;

        long totalUsers = userRepository.count();
        long totalTours = tourRepository.count();
        long totalSchedules = tourScheduleRepository.count();

        return new OverviewReport(
                start,
                end,
                totalBookings,
                confirmedBookings,
                cancelledBookings,
                totalRevenue,
                totalParticipants,
                averageBookingValue,
                conversionRate,
                totalUsers,
                totalTours,
                totalSchedules
        );
    }

    public List<BookingsByDayReport> getBookingsByDay(Instant start, Instant end) {
        // Reuse the same optimized query to avoid N+1
        List<Booking> bookings = bookingRepository.findByCreatedAtBetweenWithTourInfo(start, end);

        // Only count confirmed/completed bookings
        Map<LocalDate, List<Booking>> bookingsByDay = bookings.stream()
                .filter(this::isConfirmedOrCompleted)
                .collect(Collectors.groupingBy(b ->
                        b.getCreatedAt().atZone(DateTimeUtils.CHILE_ZONE).toLocalDate()
                ));

        return bookingsByDay.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Booking> dayBookings = entry.getValue();
                    int count = dayBookings.size();
                    BigDecimal revenue = calculateTotalRevenue(dayBookings);
                    return new BookingsByDayReport(date, count, revenue);
                })
                .sorted((a, b) -> a.date().compareTo(b.date()))
                .collect(Collectors.toList());
    }

    public List<TopTourReport> getTopTours(Instant start, Instant end, int limit) {
        // Use query with JOIN FETCH to avoid N+1 when accessing tour names
        List<Booking> bookings = bookingRepository.findByCreatedAtBetweenWithTourInfo(start, end);

        // Only count confirmed/completed bookings for top tours
        Map<String, List<Booking>> bookingsByTour = bookings.stream()
                .filter(this::isConfirmedOrCompleted)
                .filter(b -> b.getSchedule() != null && b.getSchedule().getTour() != null)
                .collect(Collectors.groupingBy(b -> TourUtils.getTourName(b.getSchedule().getTour())));

        return bookingsByTour.entrySet().stream()
                .map(entry -> {
                    String tourName = entry.getKey();
                    List<Booking> tourBookings = entry.getValue();
                    int bookingsCount = tourBookings.size();
                    BigDecimal revenue = calculateTotalRevenue(tourBookings);
                    long participants = countTotalParticipants(tourBookings);
                    return new TopTourReport(tourName, bookingsCount, revenue, participants);
                })
                .sorted((a, b) -> Integer.compare(b.bookingsCount(), a.bookingsCount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public FinancialReport getFinancialReport(Instant start, Instant end) {
        List<Payment> payments = paymentRepository.findCompletedBetween(start, end);

        // Mercado Pago - real data from API
        BigDecimal mpGross = BigDecimal.ZERO;
        BigDecimal mpFees = BigDecimal.ZERO;
        BigDecimal mpNet = BigDecimal.ZERO;
        long mpCount = 0;

        // Transbank - estimated data
        BigDecimal tbGross = BigDecimal.ZERO;
        BigDecimal tbFeesEstimated = BigDecimal.ZERO;
        long tbCount = 0;

        // Tax collected from bookings
        BigDecimal totalTax = BigDecimal.ZERO;

        for (Payment payment : payments) {
            BigDecimal amount = payment.getAmount();
            Map<String, Object> response = payment.getProviderResponse();

            if (payment.getProvider() == PaymentProvider.MERCADOPAGO) {
                mpGross = mpGross.add(amount);
                mpCount++;

                // Read net_received_amount from providerResponse
                if (response != null && response.get("net_received_amount") != null) {
                    try {
                        BigDecimal netReceived = new BigDecimal(response.get("net_received_amount").toString());
                        mpNet = mpNet.add(netReceived);
                    } catch (NumberFormatException e) {
                        log.warn("Could not parse net_received_amount for payment {}", payment.getId());
                        mpNet = mpNet.add(amount); // Fallback to gross
                    }
                } else {
                    mpNet = mpNet.add(amount); // Fallback if no data
                }

                // Read gateway_fee if available
                if (response != null && response.get("gateway_fee") != null) {
                    try {
                        BigDecimal fee = new BigDecimal(response.get("gateway_fee").toString());
                        mpFees = mpFees.add(fee);
                    } catch (NumberFormatException e) {
                        log.warn("Could not parse gateway_fee for payment {}", payment.getId());
                    }
                }

            } else if (payment.getProvider() == PaymentProvider.TRANSBANK) {
                tbGross = tbGross.add(amount);
                tbCount++;

                // Estimate fee based on payment_type_code
                BigDecimal estimatedFee = estimateTransbankFee(payment, response);
                tbFeesEstimated = tbFeesEstimated.add(estimatedFee);
            }

            // Collect tax from booking
            if (payment.getBooking() != null && payment.getBooking().getTaxAmount() != null) {
                totalTax = totalTax.add(payment.getBooking().getTaxAmount());
            }
        }

        // If we didn't get fees from API, calculate from gross - net
        if (mpFees.compareTo(BigDecimal.ZERO) == 0 && mpGross.compareTo(mpNet) > 0) {
            mpFees = mpGross.subtract(mpNet);
        }

        // Calculate Transbank estimated net
        BigDecimal tbEstimatedNet = tbGross.subtract(tbFeesEstimated);

        return FinancialReport.create(
                start,
                end,
                mpGross,
                mpFees,
                mpNet,
                mpCount,
                tbGross,
                tbFeesEstimated,
                tbEstimatedNet,
                tbCount,
                totalTax
        );
    }

    private BigDecimal estimateTransbankFee(Payment payment, Map<String, Object> response) {
        BigDecimal amount = payment.getAmount();
        var fees = paymentProperties.getTransbank().getFees();
        BigDecimal feeRate = BigDecimal.valueOf(fees.getCredit()); // Default to credit

        if (response != null && response.get("payment_type_code") != null) {
            String paymentType = response.get("payment_type_code").toString();
            feeRate = switch (paymentType) {
                case "VD" -> BigDecimal.valueOf(fees.getDebit());      // Venta Débito
                case "VP" -> BigDecimal.valueOf(fees.getPrepaid());    // Venta Prepago
                case "VN", "VC", "SI", "S2", "NC", "NP" -> BigDecimal.valueOf(fees.getCredit()); // Credit variants
                default -> BigDecimal.valueOf(fees.getCredit());
            };
        }

        return amount.multiply(feeRate).setScale(0, RoundingMode.HALF_UP);
    }

    public Instant parseStartDate(String startDate) {
        return startDate != null
                ? LocalDate.parse(startDate).atStartOfDay(DateTimeUtils.CHILE_ZONE).toInstant()
                : Instant.now().minus(DEFAULT_PERIOD_DAYS, ChronoUnit.DAYS);
    }

    public Instant parseEndDate(String endDate) {
        return endDate != null
                ? LocalDate.parse(endDate).plusDays(1).atStartOfDay(DateTimeUtils.CHILE_ZONE).toInstant()
                : Instant.now();
    }

    private long countConfirmedBookings(List<Booking> bookings) {
        return bookings.stream()
                .filter(this::isConfirmedOrCompleted)
                .count();
    }

    private long countCancelledBookings(List<Booking> bookings) {
        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();
    }

    private BigDecimal calculateTotalRevenue(List<Booking> bookings) {
        return bookings.stream()
                .filter(this::isConfirmedOrCompleted)
                .map(Booking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long countTotalParticipants(List<Booking> bookings) {
        return bookings.stream()
                .filter(this::isConfirmedOrCompleted)
                .mapToLong(b -> b.getParticipants() != null ? b.getParticipants().size() : 0)
                .sum();
    }

    private boolean isConfirmedOrCompleted(Booking booking) {
        BookingStatus status = booking.getStatus();
        return status == BookingStatus.CONFIRMED || status == BookingStatus.COMPLETED;
    }
}
