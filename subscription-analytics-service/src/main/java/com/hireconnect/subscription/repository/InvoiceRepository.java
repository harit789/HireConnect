package com.hireconnect.subscription.repository;

import com.hireconnect.subscription.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByRecruiterId(Long recruiterId);

    List<Invoice> findBySubscriptionId(Long subscriptionId);

    List<Invoice> findByPaymentMode(String paymentMode);

    Optional<Invoice> findByTransactionId(String transactionId);

    @Query("SELECT i FROM Invoice i WHERE i.recruiterId = :recruiterId " +
           "ORDER BY i.paymentDate DESC")
    List<Invoice> findLatestByRecruiterId(@Param("recruiterId") Long recruiterId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.paymentStatus = 'PAID'")
    Double getTotalPaidAmount();

    long countByPaymentStatus(String paymentStatus);
}
