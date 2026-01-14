package com.northernchile.api.booking;

import com.northernchile.api.audit.AuditLogService;
import com.northernchile.api.booking.dto.BookingRefundDetail;
import com.northernchile.api.booking.dto.CancellationReason;
import com.northernchile.api.booking.dto.ScheduleCancellationResult;
import com.northernchile.api.exception.ResourceNotFoundException;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.notification.EmailService;
import com.northernchile.api.payment.RefundService;
import com.northernchile.api.payment.dto.RefundRes;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.tour.TourUtils;
import com.northernchile.api.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for cancelling a tour schedule with cascade refunds for all associated bookings.
 * When a schedule is cancelled due to weather, operational reasons, etc., this service:
 * 1. Cancels the schedule
 * 2. Processes refunds for all confirmed bookings
 * 3. Sends cancellation emails to affected customers
 * 4. Logs audit trail
 */
@Service
public class ScheduleCancellationService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleCancellationService.class);

    private final TourScheduleRepository tourScheduleRepository;
    private final BookingRepository bookingRepository;
    private final RefundService refundService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    public ScheduleCancellationService(
            TourScheduleRepository tourScheduleRepository,
            BookingRepository bookingRepository,
            RefundService refundService,
            EmailService emailService,
            AuditLogService auditLogService) {
        this.tourScheduleRepository = tourScheduleRepository;
        this.bookingRepository = bookingRepository;
        this.refundService = refundService;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
    }

    /**
     * Cancel a schedule and process refunds for all confirmed bookings.
     * This is a "best effort" operation - if some refunds fail, we continue with the rest.
     *
     * @param scheduleId ID of the schedule to cancel
     * @param reason Reason for cancellation (WEATHER, ASTRONOMICAL, ADMIN_DECISION, OTHER)
     * @param currentUser Admin performing the cancellation
     * @return Result with details of each booking processed
     */
    @Transactional
    public ScheduleCancellationResult cancelScheduleWithRefunds(
            UUID scheduleId,
            CancellationReason reason,
            User currentUser) {

        log.info("Starting schedule cancellation with refunds: scheduleId={}, reason={}, admin={}",
                scheduleId, reason, currentUser.getEmail());

        // Load schedule
        TourSchedule schedule = tourScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("TourSchedule", scheduleId));

        // Check if already cancelled
        if ("CANCELLED".equals(schedule.getStatus())) {
            log.warn("Schedule {} is already cancelled", scheduleId);
            return new ScheduleCancellationResult(
                    scheduleId,
                    TourUtils.getTourName(schedule.getTour(), "es"),
                    schedule.getStartDatetime(),
                    reason,
                    0, 0, 0, BigDecimal.ZERO,
                    List.of()
            );
        }

        // Cancel the schedule first
        String oldStatus = schedule.getStatus();
        schedule.setStatus("CANCELLED");
        tourScheduleRepository.save(schedule);

        // Get tour name for emails and logs
        String tourName = TourUtils.getTourName(schedule.getTour(), "es");

        // Log schedule cancellation
        auditLogService.logUpdate(
                currentUser,
                "TOUR_SCHEDULE",
                scheduleId,
                tourName + " - Cancelled with cascade refunds",
                Map.of("status", oldStatus, "reason", reason.name()),
                Map.of("status", "CANCELLED")
        );

        // Load all bookings for this schedule
        List<Booking> bookings = bookingRepository.findByScheduleIdWithDetails(scheduleId);
        log.info("Found {} bookings for schedule {}", bookings.size(), scheduleId);

        // Process refunds for each booking
        List<BookingRefundDetail> refundDetails = new ArrayList<>();
        int refundsProcessed = 0;
        int refundsFailed = 0;
        BigDecimal totalRefundedAmount = BigDecimal.ZERO;

        for (Booking booking : bookings) {
            BookingRefundDetail detail = processBookingRefund(booking, reason, currentUser, tourName);
            refundDetails.add(detail);

            if (BookingRefundDetail.STATUS_SUCCESS.equals(detail.status())) {
                refundsProcessed++;
                totalRefundedAmount = totalRefundedAmount.add(detail.refundAmount());
            } else if (BookingRefundDetail.STATUS_FAILED.equals(detail.status())) {
                refundsFailed++;
            }
            // NO_PAYMENT and ALREADY_CANCELLED don't count as failed
        }

        log.info("Schedule cancellation completed: scheduleId={}, totalBookings={}, refundsProcessed={}, refundsFailed={}, totalRefunded={}",
                scheduleId, bookings.size(), refundsProcessed, refundsFailed, totalRefundedAmount);

        return new ScheduleCancellationResult(
                scheduleId,
                tourName,
                schedule.getStartDatetime(),
                reason,
                bookings.size(),
                refundsProcessed,
                refundsFailed,
                totalRefundedAmount,
                refundDetails
        );
    }

    /**
     * Process refund for a single booking and send cancellation email.
     */
    private BookingRefundDetail processBookingRefund(
            Booking booking,
            CancellationReason reason,
            User currentUser,
            String tourName) {

        UUID bookingId = booking.getId();
        String customerName = booking.getUser() != null ? booking.getUser().getFullName() : "Unknown";
        String customerEmail = booking.getUser() != null ? booking.getUser().getEmail() : null;

        log.info("Processing refund for booking: {}, customer: {}", bookingId, customerEmail);

        // Skip if already cancelled
        if ("CANCELLED".equals(booking.getStatus())) {
            log.info("Booking {} is already cancelled, skipping", bookingId);
            return BookingRefundDetail.alreadyCancelled(bookingId, customerName, customerEmail);
        }

        // Skip if not confirmed (PENDING bookings haven't paid yet)
        if (!"CONFIRMED".equals(booking.getStatus())) {
            log.info("Booking {} is not CONFIRMED (status={}), marking as cancelled without refund",
                    bookingId, booking.getStatus());
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);

            // Send cancellation email even for non-confirmed bookings
            sendCancellationEmail(booking, reason, BigDecimal.ZERO, tourName);
            return BookingRefundDetail.noPayment(bookingId, customerName, customerEmail);
        }

        // Process refund via RefundService (with admin override = true to bypass 24h rule)
        try {
            RefundRes refundResult = refundService.refundBooking(bookingId, true);

            // Send cancellation email (RefundService already sends refund confirmation,
            // but we want a specific "tour cancelled" email with reason)
            sendCancellationEmail(booking, reason, refundResult.refundAmount(), tourName);

            return BookingRefundDetail.success(
                    bookingId,
                    customerName,
                    customerEmail,
                    refundResult.refundAmount()
            );

        } catch (Exception e) {
            log.error("Failed to process refund for booking {}: {}", bookingId, e.getMessage(), e);

            // Mark booking as cancelled even if refund failed (admin will handle manually)
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);

            // Send cancellation email noting refund issue
            sendCancellationEmailWithRefundPending(booking, reason, tourName);

            return BookingRefundDetail.failed(bookingId, customerName, customerEmail, e.getMessage());
        }
    }

    /**
     * Send tour cancellation email to customer.
     */
    private void sendCancellationEmail(
            Booking booking,
            CancellationReason reason,
            BigDecimal refundAmount,
            String tourName) {

        if (booking.getUser() == null || booking.getUser().getEmail() == null) {
            log.warn("Cannot send cancellation email for booking {} - no user email", booking.getId());
            return;
        }

        try {
            String languageCode = booking.getLanguageCode() != null ? booking.getLanguageCode() : "es";
            String tourDate = DateTimeUtils.formatForDisplay(
                    booking.getSchedule().getStartDatetime(), "dd/MM/yyyy");
            String refundAmountStr = refundAmount.compareTo(BigDecimal.ZERO) > 0
                    ? String.format("CLP %,.0f", refundAmount)
                    : "N/A";

            emailService.sendBookingCancelledEmail(
                    booking.getUser().getEmail(),
                    booking.getUser().getFullName(),
                    booking.getId().toString(),
                    tourName,
                    tourDate,
                    booking.getParticipants() != null ? booking.getParticipants().size() : 0,
                    getReasonMessage(reason, languageCode),
                    refundAmountStr,
                    languageCode
            );

            log.info("Sent cancellation email to {} for booking {}", booking.getUser().getEmail(), booking.getId());

        } catch (Exception e) {
            log.error("Failed to send cancellation email for booking {}: {}", booking.getId(), e.getMessage());
        }
    }

    /**
     * Send cancellation email noting that refund is pending manual processing.
     */
    private void sendCancellationEmailWithRefundPending(
            Booking booking,
            CancellationReason reason,
            String tourName) {

        if (booking.getUser() == null || booking.getUser().getEmail() == null) {
            return;
        }

        try {
            String languageCode = booking.getLanguageCode() != null ? booking.getLanguageCode() : "es";
            String tourDate = DateTimeUtils.formatForDisplay(
                    booking.getSchedule().getStartDatetime(), "dd/MM/yyyy");

            // Send with "Refund pending" message
            String refundPendingMsg = languageCode.startsWith("en")
                    ? "Refund pending - we will contact you shortly"
                    : languageCode.startsWith("pt")
                    ? "Reembolso pendente - entraremos em contato em breve"
                    : "Reembolso pendiente - nos comunicaremos pronto";

            emailService.sendBookingCancelledEmail(
                    booking.getUser().getEmail(),
                    booking.getUser().getFullName(),
                    booking.getId().toString(),
                    tourName,
                    tourDate,
                    booking.getParticipants() != null ? booking.getParticipants().size() : 0,
                    getReasonMessage(reason, languageCode),
                    refundPendingMsg,
                    languageCode
            );

        } catch (Exception e) {
            log.error("Failed to send cancellation email for booking {}: {}", booking.getId(), e.getMessage());
        }
    }

    /**
     * Get localized cancellation reason message.
     */
    private String getReasonMessage(CancellationReason reason, String languageCode) {
        boolean isEnglish = languageCode != null && languageCode.startsWith("en");
        boolean isPortuguese = languageCode != null && languageCode.startsWith("pt");

        return switch (reason) {
            case WEATHER -> isEnglish ? "Cancelled due to weather conditions"
                    : isPortuguese ? "Cancelado devido a condições climáticas"
                    : "Cancelado por condiciones climáticas";
            case ASTRONOMICAL -> isEnglish ? "Cancelled due to astronomical conditions (full moon)"
                    : isPortuguese ? "Cancelado devido a condições astronômicas (lua cheia)"
                    : "Cancelado por condiciones astronómicas (luna llena)";
            case ADMIN_DECISION -> isEnglish ? "Cancelled due to operational reasons"
                    : isPortuguese ? "Cancelado por motivos operacionais"
                    : "Cancelado por motivos operacionales";
            case OTHER -> isEnglish ? "Tour cancelled"
                    : isPortuguese ? "Tour cancelado"
                    : "Tour cancelado";
        };
    }
}
