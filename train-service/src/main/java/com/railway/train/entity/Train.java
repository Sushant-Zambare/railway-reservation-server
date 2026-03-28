package com.railway.train.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "trains")
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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