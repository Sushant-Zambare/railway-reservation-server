package com.railway.train.controller;

import com.railway.train.entity.Train;
import com.railway.train.service.TrainService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trains")
public class TrainController {

    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Train addTrain(@RequestBody Train train) {
        return trainService.addTrain(train);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Train updateTrain(@PathVariable("id") Long id, @RequestBody Train train) {
        return trainService.updateTrain(id, train);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteTrain(@PathVariable("id") Long id) {
        return trainService.deleteTrain(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Train> getAllTrains() {
        return trainService.getAllTrains();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Train> searchTrains(@RequestParam("source") String source,
                                    @RequestParam("destination") String destination) {
        return trainService.searchTrains(source, destination);
    }

    @PutMapping("/{id}/seats")
    public Train updateSeats(@PathVariable("id") Long id,
                             @RequestParam("seats") int seats) {
        return trainService.updateSeats(id, seats);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Train getTrainById(@PathVariable("id") Long id) {
        return trainService.getTrainById(id);
    }
}