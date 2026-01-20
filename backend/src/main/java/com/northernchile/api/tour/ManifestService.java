package com.northernchile.api.tour;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.BookingStatus;
import com.northernchile.api.model.Participant;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.notification.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.northernchile.api.util.DateTimeUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service responsible for generating and sending tour manifests
 * when a schedule is closed (2 hours before tour start).
 */
@Service
public class ManifestService {

    private static final Logger log = LoggerFactory.getLogger(ManifestService.class);

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    @Value("${manifest.operator-email:contacto@northernchile.com}")
    private String operatorEmail;

    @Value("${manifest.emergency-contact:+56 9 5765 5764}")
    private String emergencyContact;

    public ManifestService(BookingRepository bookingRepository, EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
    }

    /**
     * Generates and sends the manifest for a closed schedule.
     * Also sends reminder emails to all participants with email addresses.
     */
    @Transactional(readOnly = true)
    public void generateAndSendManifest(TourSchedule schedule) {
        log.info("Generating manifest for schedule {} - Tour: {}", 
                schedule.getId(), schedule.getTour().getDisplayName());

        // Get all CONFIRMED bookings for this schedule
        List<Booking> confirmedBookings = bookingRepository.findByScheduleId(schedule.getId())
                .stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .toList();

        if (confirmedBookings.isEmpty()) {
            log.info("No confirmed bookings for schedule {}, skipping manifest", schedule.getId());
            return;
        }

        // Collect all participants
        List<ParticipantInfo> allParticipants = new ArrayList<>();
        Set<String> sentEmails = new HashSet<>(); // To avoid duplicate emails

        for (Booking booking : confirmedBookings) {
            for (Participant participant : booking.getParticipants()) {
                allParticipants.add(new ParticipantInfo(participant, booking));
            }
        }

        log.info("Found {} participants across {} bookings for schedule {}", 
                allParticipants.size(), confirmedBookings.size(), schedule.getId());

        // Send manifest to operator
        sendManifestToOperator(schedule, allParticipants);

        // Send reminders to participants with email
        for (ParticipantInfo info : allParticipants) {
            String email = info.participant.getEmail();
            if (email != null && !email.isBlank() && !sentEmails.contains(email.toLowerCase())) {
                sendReminderToParticipant(info, schedule);
                sentEmails.add(email.toLowerCase());
            }
        }

        log.info("Manifest sent to operator and {} participant reminders sent", sentEmails.size());
    }

    private void sendManifestToOperator(TourSchedule schedule, List<ParticipantInfo> participants) {
        String tourName = schedule.getTour().getNameTranslations().getOrDefault("es", 
                schedule.getTour().getDisplayName());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(DateTimeUtils.CHILE_ZONE);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                .withZone(DateTimeUtils.CHILE_ZONE);

        String tourDate = dateFormatter.format(schedule.getStartDatetime());
        String tourTime = timeFormatter.format(schedule.getStartDatetime());

        String guideName = schedule.getAssignedGuide() != null 
                ? schedule.getAssignedGuide().getFullName() 
                : "Sin asignar";

        emailService.sendManifestEmail(
                operatorEmail,
                tourName,
                tourDate,
                tourTime,
                guideName,
                participants.size(),
                buildParticipantsList(participants)
        );
    }

    private void sendReminderToParticipant(ParticipantInfo info, TourSchedule schedule) {
        String languageCode = info.booking.getLanguageCode();
        if (languageCode == null) languageCode = "es";

        String tourName = schedule.getTour().getNameTranslations().getOrDefault(
                languageCode.substring(0, 2), // "es-CL" -> "es"
                schedule.getTour().getDisplayName()
        );

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(DateTimeUtils.CHILE_ZONE);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                .withZone(DateTimeUtils.CHILE_ZONE);

        String tourDate = dateFormatter.format(schedule.getStartDatetime());
        String tourTime = timeFormatter.format(schedule.getStartDatetime());
        String pickupLocation = info.participant.getPickupAddress();

        // Get equipment list from tour
        String equipment = "";
        var equipmentTranslations = schedule.getTour().getEquipmentTranslations();
        if (equipmentTranslations != null) {
            Object equipmentObj = equipmentTranslations.getOrDefault(
                    languageCode.substring(0, 2), 
                    equipmentTranslations.getOrDefault("es", "")
            );
            equipment = equipmentObj != null ? equipmentObj.toString() : "";
        }

        emailService.sendPickupReminderToParticipant(
                info.participant.getEmail(),
                info.participant.getFullName(),
                tourName,
                tourDate,
                tourTime,
                pickupLocation != null ? pickupLocation : "",
                equipment,
                emergencyContact,
                languageCode
        );
    }

    private List<ManifestParticipant> buildParticipantsList(List<ParticipantInfo> participants) {
        List<ManifestParticipant> result = new ArrayList<>();
        int index = 1;
        
        for (ParticipantInfo info : participants) {
            Participant p = info.participant;
            result.add(new ManifestParticipant(
                    index++,
                    p.getFullName(),
                    p.getPhoneNumber(),
                    p.getPickupAddress(),
                    p.getNationality(),
                    p.getDocumentId(),
                    p.getSpecialRequirements(),
                    info.booking.getSpecialRequests()
            ));
        }
        
        return result;
    }

    /**
     * Internal record to hold participant with its booking context
     */
    private record ParticipantInfo(Participant participant, Booking booking) {}

    /**
     * DTO for manifest participant data
     */
    public record ManifestParticipant(
            int number,
            String fullName,
            String phoneNumber,
            String pickupAddress,
            String nationality,
            String documentId,
            String specialRequirements,
            String bookingSpecialRequests
    ) {}
}
