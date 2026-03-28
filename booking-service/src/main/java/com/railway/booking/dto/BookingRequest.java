package com.railway.booking.dto;

import lombok.Data;

@Data
public class BookingRequest {
    private Long trainId;
    private int seatsBooked;
}