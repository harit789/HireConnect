package com.hireconnect.subscription.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hireconnect.subscription.entity.Invoice;
import com.hireconnect.subscription.entity.Subscription;
import com.hireconnect.subscription.entity.Wallet;
import com.hireconnect.subscription.repository.InvoiceRepository;
import com.hireconnect.subscription.repository.SubscriptionRepository;
import com.hireconnect.subscription.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepo;

    @Mock
    private InvoiceRepository invoiceRepo;

    @Mock
    private WalletRepository walletRepo;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Subscription mockSub;
    private Wallet mockWallet;

    @BeforeEach
    void setUp() {
        mockSub = Subscription.builder()
                .subscriptionId(1L)
                .recruiterId(10L)
                .plan("PROFESSIONAL")
                .status("ACTIVE")
                .amountPaid(99.99)
                .build();

        mockWallet = new Wallet(10L, 100.0);
    }

    @Test
    void subscribe_Success() {
        when(subscriptionRepo.findActiveByRecruiterId(10L)).thenReturn(Optional.empty());
        when(subscriptionRepo.save(any(Subscription.class))).thenReturn(mockSub);
        when(subscriptionRepo.findById(1L)).thenReturn(Optional.of(mockSub));
        when(invoiceRepo.save(any(Invoice.class))).thenReturn(new Invoice());

        Subscription saved = subscriptionService.subscribe(10L, "PROFESSIONAL", "CARD", 99.99);

        assertNotNull(saved);
        assertEquals("PROFESSIONAL", saved.getPlan());
        verify(subscriptionRepo, times(1)).save(any(Subscription.class));
        verify(invoiceRepo, times(1)).save(any(Invoice.class));
    }

    @Test
    void subscribe_WithWallet_Success() {
        when(walletRepo.findById(10L)).thenReturn(Optional.of(mockWallet));
        when(walletRepo.save(any(Wallet.class))).thenReturn(mockWallet);
        when(subscriptionRepo.findActiveByRecruiterId(10L)).thenReturn(Optional.empty());
        when(subscriptionRepo.save(any(Subscription.class))).thenReturn(mockSub);
        when(subscriptionRepo.findById(1L)).thenReturn(Optional.of(mockSub));
        when(invoiceRepo.save(any(Invoice.class))).thenReturn(new Invoice());

        Subscription saved = subscriptionService.subscribe(10L, "WALLET", "WALLET", 50.0);

        assertNotNull(saved);
        assertEquals(50.0, mockWallet.getBalance());
    }

    @Test
    void subscribe_WithWallet_InsufficientBalance_ThrowsException() {
        when(walletRepo.findById(10L)).thenReturn(Optional.of(mockWallet));

        assertThrows(RuntimeException.class, () ->
            subscriptionService.subscribe(10L, "PROFESSIONAL", "WALLET", 150.0)
        );
    }

    @Test
    void getActiveSubscription_Success() {
        when(subscriptionRepo.findActiveByRecruiterId(10L)).thenReturn(Optional.of(mockSub));

        Subscription found = subscriptionService.getActiveSubscription(10L);

        assertNotNull(found);
        assertEquals("ACTIVE", found.getStatus());
    }

    @Test
    void cancelSubscription_Success() {
        when(subscriptionRepo.findActiveByRecruiterId(10L)).thenReturn(Optional.of(mockSub));
        when(subscriptionRepo.save(any(Subscription.class))).thenReturn(mockSub);

        Subscription cancelled = subscriptionService.cancelSubscription(10L);

        assertEquals("CANCELLED", cancelled.getStatus());
        assertNotNull(cancelled.getEndDate());
    }

    @Test
    void getWalletBalance_Success() {
        when(walletRepo.findById(10L)).thenReturn(Optional.of(mockWallet));

        Wallet balance = subscriptionService.getWalletBalance(10L);

        assertNotNull(balance);
        assertEquals(100.0, balance.getBalance());
    }

    @Test
    void addMoneyToWallet_Success() {
        when(walletRepo.findById(10L)).thenReturn(Optional.of(mockWallet));
        when(walletRepo.save(any(Wallet.class))).thenReturn(mockWallet);

        Wallet updated = subscriptionService.addMoneyToWallet(10L, 50.0);

        assertEquals(150.0, updated.getBalance());
    }

    @Test
    void deductFromWallet_Success() {
        when(walletRepo.findById(10L)).thenReturn(Optional.of(mockWallet));
        when(walletRepo.save(any(Wallet.class))).thenReturn(mockWallet);

        boolean result = subscriptionService.deductFromWallet(10L, 30.0);

        assertTrue(result);
        assertEquals(70.0, mockWallet.getBalance());
    }

    @Test
    void deductFromWallet_Failure() {
        when(walletRepo.findById(10L)).thenReturn(Optional.of(mockWallet));

        boolean result = subscriptionService.deductFromWallet(10L, 150.0);

        assertFalse(result);
        assertEquals(100.0, mockWallet.getBalance());
    }
}
