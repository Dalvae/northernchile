package com.northernchile.api.privatetour;

import com.northernchile.api.config.NotificationConfig;
import com.northernchile.api.model.PrivateTourRequest;
import com.northernchile.api.notification.EmailService;
import org.springframework.stereotype.Service;

@Service
public class PrivateTourRequestService {

    private final PrivateTourRequestRepository privateTourRequestRepository;
    private final EmailService emailService;
    private final NotificationConfig notificationConfig;

    public PrivateTourRequestService(
            PrivateTourRequestRepository privateTourRequestRepository,
            EmailService emailService,
            NotificationConfig notificationConfig) {
        this.privateTourRequestRepository = privateTourRequestRepository;
        this.emailService = emailService;
        this.notificationConfig = notificationConfig;
    }

    public PrivateTourRequest createRequest(PrivateTourRequest request) {
        PrivateTourRequest savedRequest = privateTourRequestRepository.save(request);
        emailService.sendNewPrivateRequestNotificationToAdmin(savedRequest, notificationConfig.getAdminEmail());
        return savedRequest;
    }
}
