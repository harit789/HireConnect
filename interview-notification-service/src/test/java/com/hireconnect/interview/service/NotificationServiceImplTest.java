package com.hireconnect.interview.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.hireconnect.interview.entity.Notification;
import com.hireconnect.interview.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepo;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification mockNotification;

    @BeforeEach
    void setUp() {
        mockNotification = Notification.builder()
                .notificationId(1L)
                .userId(100L)
                .type("INTERVIEW_SCHEDULED")
                .message("Test Message")
                .isRead(false)
                .build();
    }

    @Test
    void createNotification_Success() {
        when(notificationRepo.save(any(Notification.class))).thenReturn(mockNotification);

        Notification created = notificationService.createNotification(
                100L, "INTERVIEW_SCHEDULED", "Test Message", 1L, "INTERVIEW"
        );

        assertNotNull(created);
        assertEquals("Test Message", created.getMessage());
        verify(notificationRepo, times(1)).save(any(Notification.class));
    }

    @Test
    void getByUser_Success() {
        when(notificationRepo.findByUserIdOrderByCreatedAtDesc(100L))
                .thenReturn(Arrays.asList(mockNotification));

        List<Notification> results = notificationService.getByUser(100L);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepo.findById(1L)).thenReturn(Optional.of(mockNotification));
        when(notificationRepo.save(any(Notification.class))).thenReturn(mockNotification);

        Notification readNotification = notificationService.markAsRead(1L);

        assertTrue(readNotification.getIsRead());
    }

    @Test
    void markAllAsRead_Success() {
        when(notificationRepo.markAllAsRead(100L)).thenReturn(5);

        int updatedCount = notificationService.markAllAsRead(100L);

        assertEquals(5, updatedCount);
    }

    @Test
    void sendEmailAsync_WithMailSender_Success() {

        ReflectionTestUtils.setField(notificationService, "mailSender", mailSender);
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        notificationService.sendEmailAsync("test@example.com", "Subject", "Body");

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmailAsync_WithoutMailSender_DoesNotThrow() {
        ReflectionTestUtils.setField(notificationService, "mailSender", null);

        assertDoesNotThrow(() -> notificationService.sendEmailAsync("test@example.com", "Subject", "Body"));
    }
}
