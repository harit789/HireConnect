package com.hireconnect.subscription.resource;

import com.hireconnect.subscription.entity.Invoice;
import com.hireconnect.subscription.entity.Subscription;
import com.hireconnect.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionResource {

    private final SubscriptionService subscriptionService;

    @PostMapping("/subscribe")
    public ResponseEntity<Subscription> subscribe(@RequestBody Map<String, Object> body) {
        Long   recruiterId  = toLong(body.get("recruiterId"));
        String plan         = (String) body.get("plan");
        String paymentMode  = (String) body.getOrDefault("paymentMode", "CARD");
        Double amount       = toDouble(body.get("amount"));
        return ResponseEntity.ok(
                subscriptionService.subscribe(recruiterId, plan, paymentMode, amount));
    }

    @GetMapping("/recruiter/{recruiterId}/active")
    public ResponseEntity<Subscription> getActive(@PathVariable Long recruiterId) {
        return ResponseEntity.ok(subscriptionService.getActiveSubscription(recruiterId));
    }

    @GetMapping("/recruiter/{recruiterId}")
    public ResponseEntity<List<Subscription>> getAllByRecruiter(
            @PathVariable Long recruiterId) {
        return ResponseEntity.ok(subscriptionService.getAllByRecruiter(recruiterId));
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<Subscription> getById(@PathVariable Long subscriptionId) {
        return ResponseEntity.ok(subscriptionService.getById(subscriptionId));
    }

    @PatchMapping("/recruiter/{recruiterId}/cancel")
    public ResponseEntity<Subscription> cancel(@PathVariable Long recruiterId) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(recruiterId));
    }

    @PostMapping("/recruiter/{recruiterId}/renew")
    public ResponseEntity<Subscription> renew(
            @PathVariable Long recruiterId,
            @RequestBody Map<String, Object> body) {
        String plan        = (String) body.get("plan");
        String paymentMode = (String) body.getOrDefault("paymentMode", "CARD");
        Double amount      = toDouble(body.get("amount"));
        return ResponseEntity.ok(
                subscriptionService.renewSubscription(recruiterId, plan, paymentMode, amount));
    }

    @GetMapping("/plan-info/{plan}")
    public ResponseEntity<Map<String, Object>> getPlanInfo(@PathVariable String plan) {
        int limit = subscriptionService.getJobPostLimit(plan);
        return ResponseEntity.ok(Map.of(
                "plan", plan.toUpperCase(),
                "jobPostLimit", limit == Integer.MAX_VALUE ? "Unlimited" : limit,
                "features", getPlanFeatures(plan)
        ));
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        try {
            Double amount = toDouble(body.get("amount"));
            String orderJson = subscriptionService.createPaymentOrder(amount);
            return ResponseEntity.ok(orderJson);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> data) {
        try {
            String orderId = data.get("razorpay_order_id");
            String paymentId = data.get("razorpay_payment_id");
            String signature = data.get("razorpay_signature");

            boolean isValid = subscriptionService.verifyPaymentSignature(orderId, paymentId, signature);
            if (isValid) {
                return ResponseEntity.ok(Map.of("status", "Payment successful"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid signature"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Verification failed"));
        }
    }

    @GetMapping("/wallet/{userId}")
    public ResponseEntity<com.hireconnect.subscription.entity.Wallet> getWallet(@PathVariable Long userId) {
        return ResponseEntity.ok(subscriptionService.getWalletBalance(userId));
    }

    @PostMapping("/wallet/{userId}/add")
    public ResponseEntity<com.hireconnect.subscription.entity.Wallet> addMoneyToWallet(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {
        Double amount = toDouble(body.get("amount"));
        return ResponseEntity.ok(subscriptionService.addMoneyToWallet(userId, amount));
    }

    @GetMapping("/invoices/recruiter/{recruiterId}")
    public ResponseEntity<List<Invoice>> getInvoicesByRecruiter(
            @PathVariable Long recruiterId) {
        return ResponseEntity.ok(subscriptionService.getInvoicesByRecruiter(recruiterId));
    }

    @GetMapping("/invoices/{invoiceId}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(subscriptionService.getInvoiceById(invoiceId));
    }

    @GetMapping("/invoices/subscription/{subscriptionId}")
    public ResponseEntity<List<Invoice>> getInvoicesBySubscription(
            @PathVariable Long subscriptionId) {
        return ResponseEntity.ok(subscriptionService.getInvoicesBySubscription(subscriptionId));
    }

    private Long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }

    private Double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number n) return n.doubleValue();
        return Double.parseDouble(val.toString());
    }

    private List<String> getPlanFeatures(String plan) {
        return switch (plan.toUpperCase()) {
            case "FREE"         -> List.of("3 job posts", "Basic dashboard",
                                           "Email support");
            case "PROFESSIONAL" -> List.of("20 job posts/month", "Analytics dashboard",
                                           "Candidate messaging", "Priority support");
            case "ENTERPRISE"   -> List.of("Unlimited job posts", "Advanced analytics",
                                           "Team management", "API access",
                                           "Dedicated account manager");
            default             -> List.of();
        };
    }
}
