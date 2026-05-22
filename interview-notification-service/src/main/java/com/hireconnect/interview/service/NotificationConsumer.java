package com.hireconnect.interview.service;

import com.hireconnect.interview.config.RabbitMQConfig;
import com.hireconnect.interview.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void consumeNotificationEvent(NotificationEvent event) {
        System.out.println("Received notification event from RabbitMQ: " + event);

        if ("EMAIL".equalsIgnoreCase(event.getType())) {
            if (event.getUserEmail() != null && !event.getUserEmail().isBlank()) {
                notificationService.sendEmailAsync(
                        event.getUserEmail(),
                        event.getSubject(),
                        event.getMessage()
                );
            } else {
                System.out.println("No email address provided in event. Skipping email dispatch.");
            }
        }

        notificationService.createNotification(
                event.getUserId(),
                event.getType(),
                event.getMessage(),
                event.getReferenceId(),
                event.getReferenceType()
        );
    }
}
