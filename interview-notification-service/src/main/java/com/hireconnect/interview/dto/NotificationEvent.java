package com.hireconnect.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEvent {
    private Long   userId;
    private String userEmail;
    private String subject;
    private String message;
    private String type;
    private Long   referenceId;
    private String referenceType;
}
