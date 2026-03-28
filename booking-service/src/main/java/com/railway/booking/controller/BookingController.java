package com.railway.booking.controller;

import com.railway.booking.dto.BookingRequest;
import com.railway.booking.entity.Booking;
import com.railway.booking.entity.BookingStatus;
import com.railway.booking.service.BookingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Booking createBooking(@RequestBody BookingRequest request,
                                 Authentication authentication,
                                 @RequestHeader("Authorization") String authHeader) {
        String userEmail = authentication.getName();
        String token = authHeader.substring(7);
        return bookingService.createBooking(request, userEmail, token);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Booking cancelBooking(@PathVariable("id") Long id,
                                 Authentication authentication,
                                 @RequestHeader("Authorization") String authHeader) {
        String userEmail = authentication.getName();
        String token = authHeader.substring(7);
        return bookingService.cancelBooking(id, userEmail, token);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Booking getBookingById(@PathVariable("id") Long id) {
        return bookingService.getBookingById(id);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Booking> getMyBookings(Authentication authentication) {
        return bookingService.getMyBookings(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @PutMapping("/{id}/status")
    public Booking updateBookingStatus(@PathVariable("id") Long id,
                                       @RequestParam("status") BookingStatus status) {
        return bookingService.updateBookingStatus(id, status);
    }
}