package com.northernchile.api.user;

import com.northernchile.api.model.SavedParticipant;
import com.northernchile.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedParticipantRepository extends JpaRepository<SavedParticipant, UUID> {

    /**
     * Find all saved participants for a user, ordered by creation date descending.
     */
    List<SavedParticipant> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find the participant marked as "self" for a user.
     */
    Optional<SavedParticipant> findByUserAndIsSelfTrue(User user);

    /**
     * Check if user already has a "self" participant.
     */
    boolean existsByUserAndIsSelfTrue(User user);

    /**
     * Delete all saved participants for a user.
     */
    void deleteByUser(User user);
}
