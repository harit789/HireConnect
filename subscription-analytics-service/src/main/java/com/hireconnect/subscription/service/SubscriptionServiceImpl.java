package com.hireconnect.subscription.service;

import com.hireconnect.subscription.entity.Invoice;
import com.hireconnect.subscription.entity.Subscription;
import com.hireconnect.subscription.repository.InvoiceRepository;
import com.hireconnect.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepo;
    private final InvoiceRepository invoiceRepo;
    private final com.hireconnect.subscription.repository.WalletRepository walletRepo;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @Override
    @Transactional
    public Subscription subscribe(Long recruiterId, String plan,
                                  String paymentMode, Double amount) {
        if ("WALLET".equalsIgnoreCase(paymentMode)) {
            if (!deductFromWallet(recruiterId, amount)) {
                throw new RuntimeException("Insufficient wallet balance for this subscription");
            }
        }

        subscriptionRepo.findActiveByRecruiterId(recruiterId)
                .ifPresent(existing -> {
                    existing.setStatus("CANCELLED");
                    subscriptionRepo.save(existing);
                });

        Subscription sub = Subscription.builder()
                .recruiterId(recruiterId)
                .plan(plan.toUpperCase())
                .startDate(LocalDate.now())
                .endDate(computeEndDate(plan))
                .status("ACTIVE")
                .amountPaid(amount)
                .build();
        Subscription saved = subscriptionRepo.save(sub);

        generateInvoice(saved.getSubscriptionId(), paymentMode, amount,
                        UUID.randomUUID().toString());
        return saved;
    }

    @Override
    public Subscription getActiveSubscription(Long recruiterId) {
        return subscriptionRepo.findActiveByRecruiterId(recruiterId)
                .orElseThrow(() -> new RuntimeException(
                        "No active subscription for recruiter: " + recruiterId));
    }

    @Override
    public List<Subscription> getAllByRecruiter(Long recruiterId) {
        return subscriptionRepo.findByRecruiterId(recruiterId);
    }

    @Override
    @Transactional
    public Subscription cancelSubscription(Long recruiterId) {
        Subscription sub = getActiveSubscription(recruiterId);
        sub.setStatus("CANCELLED");
        sub.setEndDate(LocalDate.now());
        return subscriptionRepo.save(sub);
    }

    @Override
    @Transactional
    public Subscription renewSubscription(Long recruiterId, String plan,
                                          String paymentMode, Double amount) {

        return subscribe(recruiterId, plan, paymentMode, amount);
    }

    @Override
    public Subscription getById(Long subscriptionId) {
        return subscriptionRepo.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException(
                        "Subscription not found: " + subscriptionId));
    }

    @Override
    @Transactional
    public Invoice generateInvoice(Long subscriptionId, String paymentMode,
                                   Double amount, String transactionId) {
        Subscription sub = getById(subscriptionId);
        Invoice invoice = Invoice.builder()
                .subscriptionId(subscriptionId)
                .recruiterId(sub.getRecruiterId())
                .amount(amount)
                .paymentMode(paymentMode.toUpperCase())
                .transactionId(transactionId)
                .paymentStatus("PAID")
                .planName(sub.getPlan())
                .build();
        return invoiceRepo.save(invoice);
    }

    @Override
    public List<Invoice> getInvoicesByRecruiter(Long recruiterId) {
        return invoiceRepo.findLatestByRecruiterId(recruiterId);
    }

    @Override
    public List<Invoice> getInvoicesBySubscription(Long subscriptionId) {
        return invoiceRepo.findBySubscriptionId(subscriptionId);
    }

    @Override
    public Invoice getInvoiceById(Long invoiceId) {
        return invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException(
                        "Invoice not found: " + invoiceId));
    }

    @Override
    public int getJobPostLimit(String plan) {
        return switch (plan.toUpperCase()) {
            case "FREE"         -> 3;
            case "PROFESSIONAL" -> 20;
            case "ENTERPRISE"   -> Integer.MAX_VALUE;
            default -> 0;
        };
    }

    private LocalDate computeEndDate(String plan) {
        return switch (plan.toUpperCase()) {
            case "FREE"         -> null;
            case "PROFESSIONAL" -> LocalDate.now().plusMonths(1);
            case "ENTERPRISE"   -> LocalDate.now().plusYears(1);
            default             -> LocalDate.now().plusMonths(1);
        };
    }

    @Override
    public String createPaymentOrder(Double amount) throws Exception {
        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + UUID.randomUUID().toString());

        Order order = razorpayClient.orders.create(orderRequest);
        return order.toString();
    }

    @Override
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) throws Exception {
        String payload = orderId + "|" + paymentId;
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(razorpayKeySecret.getBytes(), "HmacSHA256");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(payload.getBytes());
        String generatedSignature = bytesToHex(hash);
        return generatedSignature.equals(signature);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public com.hireconnect.subscription.entity.Wallet getWalletBalance(Long userId) {
        return walletRepo.findById(userId)
                .orElseGet(() -> walletRepo.save(new com.hireconnect.subscription.entity.Wallet(userId, 0.0)));
    }

    @Override
    @Transactional
    public com.hireconnect.subscription.entity.Wallet addMoneyToWallet(Long userId, Double amount) {
        com.hireconnect.subscription.entity.Wallet wallet = getWalletBalance(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        return walletRepo.save(wallet);
    }

    @Override
    @Transactional
    public boolean deductFromWallet(Long userId, Double amount) {
        com.hireconnect.subscription.entity.Wallet wallet = getWalletBalance(userId);
        if (wallet.getBalance() >= amount) {
            wallet.setBalance(wallet.getBalance() - amount);
            walletRepo.save(wallet);
            return true;
        }
        return false;
    }
}
