package com.hireconnect.subscription.repository;

import com.hireconnect.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByRecruiterId(Long recruiterId);

    List<Subscription> findByStatus(String status);

    List<Subscription> findByPlan(String plan);

    @Query("SELECT s FROM Subscription s WHERE s.recruiterId = :recruiterId " +
           "AND s.status = 'ACTIVE' ORDER BY s.createdAt DESC")
    Optional<Subscription> findActiveByRecruiterId(@Param("recruiterId") Long recruiterId);

    long countByPlan(String plan);

    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(s.amountPaid), 0) FROM Subscription s WHERE s.status = 'ACTIVE'")
    Double getTotalRevenue();
}
