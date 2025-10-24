package com.northernchile.api.privatetour;

import com.northernchile.api.model.PrivateTourRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PrivateTourRequestRepository extends JpaRepository<PrivateTourRequest, UUID> {
}
