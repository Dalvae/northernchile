package com.northernchile.api.contact;

import com.northernchile.api.model.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID> {

    /**
     * Paginated message listing, sorted by creation date descending.
     */
    Page<ContactMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Paginated messages filtered by status, sorted by creation date descending.
     */
    Page<ContactMessage> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    long countByStatus(String status);
}
