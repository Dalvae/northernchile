package com.northernchile.api.privatetour;

import com.northernchile.api.model.PrivateTourRequest;
import com.northernchile.api.notification.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PrivateTourRequestService {

    private final PrivateTourRequestRepository privateTourRequestRepository;
    private final EmailService emailService;

    @Value("${mail.from.email}")
    private String adminEmail;

    public PrivateTourRequestService(
            PrivateTourRequestRepository privateTourRequestRepository,
            EmailService emailService) {
        this.privateTourRequestRepository = privateTourRequestRepository;
        this.emailService = emailService;
    }

    public PrivateTourRequest createRequest(PrivateTourRequest request) {
        PrivateTourRequest savedRequest = privateTourRequestRepository.save(request);
        emailService.sendNewPrivateRequestNotificationToAdmin(savedRequest, adminEmail);
        return savedRequest;
    }
}
