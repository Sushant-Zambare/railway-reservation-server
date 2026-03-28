package com.railway.booking.dto;

import lombok.Data;

@Data
public class TrainResponse {
    private Long id;
    private String trainNumber;
    private String trainName;
    private String source;
    private String destination;
    private String departureTime;
    private String arrivalTime;
    private int totalSeats;
    private int availableSeats;
}