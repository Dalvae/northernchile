package com.northernchile.api.user;

import com.northernchile.api.exception.ResourceNotFoundException;
import com.northernchile.api.model.SavedParticipant;
import com.northernchile.api.model.User;
import com.northernchile.api.user.dto.SavedParticipantReq;
import com.northernchile.api.user.dto.SavedParticipantRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SavedParticipantService {

    private static final Logger log = LoggerFactory.getLogger(SavedParticipantService.class);

    private final SavedParticipantRepository savedParticipantRepository;
    private final UserRepository userRepository;
    private final SavedParticipantMapper savedParticipantMapper;

    public SavedParticipantService(
            SavedParticipantRepository savedParticipantRepository,
            UserRepository userRepository,
            SavedParticipantMapper savedParticipantMapper) {
        this.savedParticipantRepository = savedParticipantRepository;
        this.userRepository = userRepository;
        this.savedParticipantMapper = savedParticipantMapper;
    }

    /**
     * Get all saved participants for a user.
     */
    @Transactional(readOnly = true)
    public List<SavedParticipantRes> getParticipantsForUser(User user) {
        List<SavedParticipant> participants = savedParticipantRepository.findByUserOrderByCreatedAtDesc(user);
        return savedParticipantMapper.toResList(participants);
    }

    /**
     * Get the "self" participant for a user (the one marked as isSelf=true).
     */
    @Transactional(readOnly = true)
    public Optional<SavedParticipantRes> getSelfParticipant(User user) {
        return savedParticipantRepository.findByUserAndIsSelfTrue(user)
                .map(savedParticipantMapper::toRes);
    }

    /**
     * Create a new saved participant for a user.
     */
    public SavedParticipantRes createParticipant(User user, SavedParticipantReq req) {
        SavedParticipant participant = new SavedParticipant();
        participant.setUser(user);
        populateFromReq(participant, req);
        participant.setSelf(false);

        SavedParticipant saved = savedParticipantRepository.save(participant);
        log.info("Created saved participant {} for user {}", saved.getId(), user.getEmail());
        return savedParticipantMapper.toRes(saved);
    }

    /**
     * Update an existing saved participant.
     */
    public SavedParticipantRes updateParticipant(UUID participantId, User user, SavedParticipantReq req) {
        SavedParticipant participant = savedParticipantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedParticipant", participantId));

        // Verify ownership
        if (!participant.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only update your own saved participants");
        }

        populateFromReq(participant, req);
        SavedParticipant saved = savedParticipantRepository.save(participant);

        // If this is the "self" participant, sync to user profile
        if (participant.isSelf()) {
            syncToUserProfile(user, participant);
        }

        log.info("Updated saved participant {} for user {}", saved.getId(), user.getEmail());
        return savedParticipantMapper.toRes(saved);
    }

    /**
     * Delete a saved participant.
     */
    public void deleteParticipant(UUID participantId, User user) {
        SavedParticipant participant = savedParticipantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedParticipant", participantId));

        // Verify ownership
        if (!participant.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only delete your own saved participants");
        }

        savedParticipantRepository.delete(participant);
        log.info("Deleted saved participant {} for user {}", participantId, user.getEmail());
    }

    /**
     * Create or update the "self" participant (the one representing the user).
     * Also syncs the data to the user's profile.
     */
    public SavedParticipantRes createOrUpdateSelf(User user, SavedParticipantReq req) {
        Optional<SavedParticipant> existingSelf = savedParticipantRepository.findByUserAndIsSelfTrue(user);

        SavedParticipant selfParticipant;
        if (existingSelf.isPresent()) {
            selfParticipant = existingSelf.get();
            populateFromReq(selfParticipant, req);
        } else {
            selfParticipant = new SavedParticipant();
            selfParticipant.setUser(user);
            selfParticipant.setSelf(true);
            populateFromReq(selfParticipant, req);
        }

        SavedParticipant saved = savedParticipantRepository.save(selfParticipant);

        // Sync data to user profile
        syncToUserProfile(user, saved);

        log.info("Created/updated self participant {} for user {}", saved.getId(), user.getEmail());
        return savedParticipantMapper.toRes(saved);
    }

    /**
     * Create a saved participant from booking participant data.
     * Used when saveForFuture is true during booking.
     */
    public SavedParticipant createFromBookingData(
            User user,
            String fullName,
            String documentId,
            java.time.LocalDate dateOfBirth,
            String nationality,
            String phoneNumber,
            String email,
            boolean markAsSelf) {

        SavedParticipant participant;

        if (markAsSelf) {
            // If marking as self, update or create the self participant
            participant = savedParticipantRepository.findByUserAndIsSelfTrue(user)
                    .orElseGet(() -> {
                        SavedParticipant newSelf = new SavedParticipant();
                        newSelf.setUser(user);
                        newSelf.setSelf(true);
                        return newSelf;
                    });
        } else {
            participant = new SavedParticipant();
            participant.setUser(user);
            participant.setSelf(false);
        }

        participant.setFullName(fullName);
        participant.setDocumentId(documentId);
        participant.setDateOfBirth(dateOfBirth);
        participant.setNationality(nationality);
        participant.setPhoneNumber(phoneNumber);
        participant.setEmail(email);

        SavedParticipant saved = savedParticipantRepository.save(participant);

        // If marking as self, sync to user profile
        if (markAsSelf) {
            syncToUserProfile(user, saved);
        }

        log.info("Created saved participant from booking data {} for user {}, isSelf={}",
                saved.getId(), user.getEmail(), markAsSelf);
        return saved;
    }

    /**
     * Sync saved participant data to user profile.
     * Only updates user fields that are currently empty/null (doesn't overwrite existing data).
     * Updates user's documentId, dateOfBirth, nationality, phoneNumber.
     */
    private void syncToUserProfile(User user, SavedParticipant participant) {
        boolean updated = false;

        // Only update if user's field is empty and participant has a value
        if (participant.getDocumentId() != null && !participant.getDocumentId().isBlank()
                && (user.getDocumentId() == null || user.getDocumentId().isBlank())) {
            user.setDocumentId(participant.getDocumentId());
            updated = true;
        }
        if (participant.getDateOfBirth() != null && user.getDateOfBirth() == null) {
            user.setDateOfBirth(participant.getDateOfBirth());
            updated = true;
        }
        if (participant.getNationality() != null && !participant.getNationality().isBlank()
                && (user.getNationality() == null || user.getNationality().isBlank())) {
            user.setNationality(participant.getNationality());
            updated = true;
        }
        if (participant.getPhoneNumber() != null && !participant.getPhoneNumber().isBlank()
                && (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank())) {
            user.setPhoneNumber(participant.getPhoneNumber());
            updated = true;
        }

        if (updated) {
            userRepository.save(user);
            log.info("Synced self participant data to user profile for {} (only empty fields)", user.getEmail());
        }
    }

    private void populateFromReq(SavedParticipant participant, SavedParticipantReq req) {
        participant.setFullName(req.fullName());
        participant.setDocumentId(req.documentId());
        participant.setDateOfBirth(req.dateOfBirth());
        participant.setNationality(req.nationality());
        participant.setPhoneNumber(req.phoneNumber());
        participant.setEmail(req.email());
    }
}
