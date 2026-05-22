package com.hireconnect.interview.service;

import com.hireconnect.interview.entity.Notification;
import com.hireconnect.interview.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.util.List;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepo;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username:haritpatel789@gmail.com}")
    private String mailFrom;

    public NotificationServiceImpl(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    @Override
    @Transactional
    public Notification createNotification(Long userId, String type, String message,
                                           Long referenceId, String referenceType) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .isRead(false)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
        return notificationRepo.save(notification);
    }

    @Override
    public List<Notification> getByUser(Long userId) {
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> getUnreadByUser(Long userId) {
        return notificationRepo.findByUserIdAndIsRead(userId, false);
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepo.countByUserIdAndIsRead(userId, false);
    }

    @Override
    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException(
                        "Notification not found: " + notificationId));
        notification.setIsRead(true);
        return notificationRepo.save(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepo.markAllAsRead(userId);
    }

    @Override
    @Transactional
    @CacheEvict(value = "notifications", allEntries = true)
    public void deleteNotification(Long notificationId) {
        notificationRepo.deleteById(notificationId);
    }

    @Override
    @Async
    public void sendEmailAsync(String toEmail, String subject, String body) {
        if (mailSender == null) {
            log.warn("Email NOT sent to {} — JavaMailSender is not configured. " +
                     "Please check your application.yml and ensure MAIL_USER/MAIL_PASS are set.", toEmail);
            return;
        }

        try {
            log.info("Attempting to send email to {} with subject: {}", toEmail, subject);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            message.setFrom(mailFrom);

            mailSender.send(message);
            log.info("SUCCESS: Email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("ERROR: Failed to send email to {}. Reason: {}", toEmail, e.getMessage(), e);
        }
    }
}
