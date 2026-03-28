package com.railway.payment.service;

import com.railway.payment.dto.BookingResponse;
import com.railway.payment.entity.Payment;
import com.railway.payment.entity.PaymentStatus;
import com.railway.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;

    @Value("${payment.price-per-seat}")
    private double pricePerSeat;

    private static final String BOOKING_SERVICE_URL = "http://BOOKING-SERVICE/bookings";

    public PaymentService(PaymentRepository paymentRepository,
                          RestTemplate restTemplate) {
        this.paymentRepository = paymentRepository;
        this.restTemplate = restTemplate;
    }

    private HttpEntity<Void> createAuthHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return new HttpEntity<>(headers);
    }

    public Payment initiatePayment(Long bookingId, String userEmail, String token) {

        paymentRepository.findByBookingId(bookingId).ifPresent(p -> {
            if (p.getStatus() == PaymentStatus.COMPLETED) {
                throw new RuntimeException("Payment already completed for booking: " + bookingId);
            }
        });

        ResponseEntity<BookingResponse> response;
        try {
            response = restTemplate.exchange(
                    BOOKING_SERVICE_URL + "/" + bookingId,
                    HttpMethod.GET,
                    createAuthHeader(token),
                    BookingResponse.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Booking not found with id: " + bookingId);
        }

        BookingResponse booking = response.getBody();

        if (booking == null) {
            throw new RuntimeException("Booking not found with id: " + bookingId);
        }

        if (booking.getStatus().equals("CANCELLED")) {
            throw new RuntimeException("Cannot pay for a cancelled booking");
        }

        if (booking.getStatus().equals("CONFIRMED")) {
            throw new RuntimeException("Booking is already confirmed and paid");
        }
        double amount = booking.getSeatsBooked() * pricePerSeat;

        Payment payment = new Payment();
        payment.setBookingId(bookingId);
        payment.setUserEmail(userEmail);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());

        try {
            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            restTemplate.exchange(
                    BOOKING_SERVICE_URL + "/" + bookingId + "/status?status=CONFIRMED",
                    HttpMethod.PUT,
                    createAuthHeader(token),
                    Void.class
            );

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.PENDING);
            paymentRepository.save(payment);

            restTemplate.exchange(
                    BOOKING_SERVICE_URL + "/" + bookingId + "/status?status=CANCELLED",
                    HttpMethod.PUT,
                    createAuthHeader(token),
                    Void.class
            );

            throw new RuntimeException("Payment failed: " + e.getMessage());
        }

        return payment;
    }

    public Payment refundPayment(Long bookingId, String userEmail) {

        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException(
                        "Payment not found for booking: " + bookingId));

        if (!payment.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("You are not authorized to refund this payment");
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new RuntimeException("Payment is already refunded");
        }

        if (payment.getStatus() == PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not completed yet");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }

    public Payment getPaymentStatus(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException(
                        "Payment not found for booking: " + bookingId));
    }

    public List<Payment> getMyPayments(String userEmail) {
        return paymentRepository.findByUserEmail(userEmail);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}