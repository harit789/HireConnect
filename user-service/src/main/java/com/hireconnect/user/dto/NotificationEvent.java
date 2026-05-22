package com.hireconnect.user.dto;

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
    private String type;            // "EMAIL" or "IN_APP"
    private Long   referenceId;     // e.g. applicationId
    private String referenceType;   // e.g. "APPLICATION"
}
