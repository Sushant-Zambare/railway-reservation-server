package com.railway.payment.controller;

import com.railway.payment.entity.Payment;
import com.railway.payment.service.PaymentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Payment initiatePayment(@PathVariable("bookingId") Long bookingId,
                                   Authentication authentication,
                                   @RequestHeader("Authorization") String authHeader) {
        String userEmail = authentication.getName();
        String token = authHeader.substring(7);
        return paymentService.initiatePayment(bookingId, userEmail, token);
    }

    @PostMapping("/{bookingId}/refund")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Payment refundPayment(@PathVariable("bookingId") Long bookingId,
                                 Authentication authentication) {
        String userEmail = authentication.getName();
        return paymentService.refundPayment(bookingId, userEmail);
    }

    @GetMapping("/status/{bookingId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Payment getPaymentStatus(@PathVariable("bookingId") Long bookingId) {
        return paymentService.getPaymentStatus(bookingId);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Payment> getMyPayments(Authentication authentication) {
        return paymentService.getMyPayments(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }
}