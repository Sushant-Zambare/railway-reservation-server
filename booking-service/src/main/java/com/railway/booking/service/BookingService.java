package com.railway.booking.service;

import com.railway.booking.dto.BookingRequest;
import com.railway.booking.dto.TrainResponse;
import com.railway.booking.entity.Booking;
import com.railway.booking.entity.BookingStatus;
import com.railway.booking.repository.BookingRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;

    private static final String TRAIN_SERVICE_URL = "http://TRAIN-SERVICE/trains";

    public BookingService(BookingRepository bookingRepository,
                          RestTemplate restTemplate) {
        this.bookingRepository = bookingRepository;
        this.restTemplate = restTemplate;
    }

    private HttpEntity<Void> createAuthHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return new HttpEntity<>(headers);
    }

    public Booking createBooking(BookingRequest request, String userEmail, String token) {

        ResponseEntity<TrainResponse> response = restTemplate.exchange(
                TRAIN_SERVICE_URL + "/" + request.getTrainId(),
                HttpMethod.GET,
                createAuthHeader(token),
                TrainResponse.class
        );
        TrainResponse train = response.getBody();

        if (train == null) {
            throw new RuntimeException("Train not found with id: " + request.getTrainId());
        }

        if (train.getAvailableSeats() < request.getSeatsBooked()) {
            throw new RuntimeException("Not enough seats available. Available: "
                    + train.getAvailableSeats());
        }

        int updatedSeats = train.getAvailableSeats() - request.getSeatsBooked();
        restTemplate.exchange(
                TRAIN_SERVICE_URL + "/" + request.getTrainId() + "/seats?seats=" + updatedSeats,
                HttpMethod.PUT,
                createAuthHeader(token),
                Void.class
        );

        Booking booking = new Booking();
        booking.setTrainId(request.getTrainId());
        booking.setUserEmail(userEmail);
        booking.setSeatsBooked(request.getSeatsBooked());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setBookingDate(LocalDateTime.now());

        return bookingRepository.save(booking);
    }

    public Booking updateBookingStatus(Long bookingId, BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public Booking cancelBooking(Long bookingId, String userEmail, String token) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (!booking.getUserEmail().equals(userEmail)) {
            throw new RuntimeException("You are not authorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }

        ResponseEntity<TrainResponse> response = restTemplate.exchange(
                TRAIN_SERVICE_URL + "/" + booking.getTrainId(),
                HttpMethod.GET,
                createAuthHeader(token),
                TrainResponse.class
        );
        TrainResponse train = response.getBody();

        if (train != null) {
            int restoredSeats = train.getAvailableSeats() + booking.getSeatsBooked();
            restTemplate.exchange(
                    TRAIN_SERVICE_URL + "/" + booking.getTrainId() + "/seats?seats=" + restoredSeats,
                    HttpMethod.PUT,
                    createAuthHeader(token),
                    Void.class
            );
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + token);
                restTemplate.exchange(
                        "http://PAYMENT-SERVICE/payments/" + bookingId + "/refund",
                        HttpMethod.POST,
                        new HttpEntity<>(headers),
                        Void.class
                );
            } catch (Exception e) {
                System.out.println("Refund call failed: " + e.getMessage());
            }
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    public List<Booking> getMyBookings(String userEmail) {
        return bookingRepository.findByUserEmail(userEmail);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
}