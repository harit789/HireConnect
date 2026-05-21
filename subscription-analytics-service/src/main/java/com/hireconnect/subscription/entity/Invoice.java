package com.hireconnect.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    @Column(nullable = false)
    private Long subscriptionId;

    @Column(nullable = false)
    private Long recruiterId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false, length = 20)
    private String paymentMode;

    @Column(unique = true, length = 100)
    private String transactionId;

    @Column(nullable = false, length = 20)
    private String paymentStatus;

    @Column(length = 20)
    private String planName;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.paymentDate == null) {
            this.paymentDate = LocalDateTime.now();
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = "PAID";
        }
    }
}
