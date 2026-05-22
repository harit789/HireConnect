package com.hireconnect.subscription.service;

import com.hireconnect.subscription.entity.Invoice;
import com.hireconnect.subscription.entity.Subscription;

import java.util.List;

public interface SubscriptionService {

    Subscription subscribe(Long recruiterId, String plan,
                           String paymentMode, Double amount);

    Subscription getActiveSubscription(Long recruiterId);

    List<Subscription> getAllByRecruiter(Long recruiterId);

    Subscription cancelSubscription(Long recruiterId);

    Subscription renewSubscription(Long recruiterId, String plan,
                                   String paymentMode, Double amount);

    Subscription getById(Long subscriptionId);

    Invoice generateInvoice(Long subscriptionId, String paymentMode,
                            Double amount, String transactionId);

    List<Invoice> getInvoicesByRecruiter(Long recruiterId);

    List<Invoice> getInvoicesBySubscription(Long subscriptionId);

    Invoice getInvoiceById(Long invoiceId);

    int getJobPostLimit(String plan);

    String createPaymentOrder(Double amount) throws Exception;

    boolean verifyPaymentSignature(String orderId, String paymentId, String signature) throws Exception;

    com.hireconnect.subscription.entity.Wallet getWalletBalance(Long userId);

    com.hireconnect.subscription.entity.Wallet addMoneyToWallet(Long userId, Double amount);

    boolean deductFromWallet(Long userId, Double amount);
}
