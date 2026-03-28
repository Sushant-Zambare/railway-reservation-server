package com.railway.payment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;
    private String userEmail;
    private double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime paymentDate;
}