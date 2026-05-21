package com.hireconnect.interview.service;

import com.hireconnect.interview.entity.Notification;

import java.util.List;

public interface NotificationService {

    Notification createNotification(Long userId, String type, String message,
                                    Long referenceId, String referenceType);

    List<Notification> getByUser(Long userId);

    List<Notification> getUnreadByUser(Long userId);

    long countUnread(Long userId);

    Notification markAsRead(Long notificationId);

    int markAllAsRead(Long userId);

    void deleteNotification(Long notificationId);

    void sendEmailAsync(String toEmail, String subject, String body);
}
