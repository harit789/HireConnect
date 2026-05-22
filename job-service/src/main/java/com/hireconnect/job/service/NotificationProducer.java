package com.hireconnect.job.service;

import com.hireconnect.job.config.RabbitMQConfig;
import com.hireconnect.job.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendNotification(NotificationEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
        System.out.println("Published notification event: " + event);
    }
}
