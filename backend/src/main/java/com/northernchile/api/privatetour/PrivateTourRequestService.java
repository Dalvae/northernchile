package com.northernchile.api.privatetour;

import com.northernchile.api.model.PrivateTourRequest;
import com.northernchile.api.notification.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrivateTourRequestService {

    @Autowired
    private PrivateTourRequestRepository privateTourRequestRepository;

    @Autowired
    private EmailService emailService;

    public PrivateTourRequest createRequest(PrivateTourRequest request) {
        PrivateTourRequest savedRequest = privateTourRequestRepository.save(request);
        emailService.sendNewPrivateRequestNotificationToAdmin(savedRequest.getId().toString());
        return savedRequest;
    }
}
