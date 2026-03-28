package com.railway.payment.dto;

import lombok.Data;

@Data
public class BookingResponse {
    private Long id;
    private Long trainId;
    private String userEmail;
    private int seatsBooked;
    private String status;
    private String bookingDate;
}