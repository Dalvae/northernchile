package com.northernchile.api.contact;

import com.northernchile.api.model.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID> {

    List<ContactMessage> findAllByOrderByCreatedAtDesc();

    /**
     * Paginated version for admin message listing.
     */
    Page<ContactMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<ContactMessage> findByStatusOrderByCreatedAtDesc(String status);

    /**
     * Paginated version filtered by status.
     */
    Page<ContactMessage> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    long countByStatus(String status);
}
