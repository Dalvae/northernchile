package com.northernchile.api.privatetour;

import com.northernchile.api.model.PrivateTourRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PrivateTourRequestRepository extends JpaRepository<PrivateTourRequest, UUID> {

    /**
     * Find all private tour requests with pagination, ordered by creation date.
     */
    Page<PrivateTourRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
