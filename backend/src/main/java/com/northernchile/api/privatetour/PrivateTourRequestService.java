package com.northernchile.api.privatetour;

import com.northernchile.api.model.PrivateTourRequest;
import com.northernchile.api.notification.EmailService;
import org.springframework.stereotype.Service;

@Service
public class PrivateTourRequestService {

    private final PrivateTourRequestRepository privateTourRequestRepository;
    private final EmailService emailService;

    public PrivateTourRequestService(
            PrivateTourRequestRepository privateTourRequestRepository,
            EmailService emailService) {
        this.privateTourRequestRepository = privateTourRequestRepository;
        this.emailService = emailService;
    }

    public PrivateTourRequest createRequest(PrivateTourRequest request) {
        PrivateTourRequest savedRequest = privateTourRequestRepository.save(request);
        emailService.sendNewPrivateRequestNotificationToAdmin(savedRequest.getId().toString());
        return savedRequest;
    }
}
