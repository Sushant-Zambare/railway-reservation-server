package com.railway.train.service;

import com.railway.train.entity.Train;
import com.railway.train.repository.TrainRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainService {

    private final TrainRepository trainRepository;

    public TrainService(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
    }

    public Train addTrain(Train train) {
        return trainRepository.save(train);
    }

    public Train updateTrain(Long id, Train updatedTrain) {
        Train existing = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));

        existing.setTrainNumber(updatedTrain.getTrainNumber());
        existing.setTrainName(updatedTrain.getTrainName());
        existing.setSource(updatedTrain.getSource());
        existing.setDestination(updatedTrain.getDestination());
        existing.setDepartureTime(updatedTrain.getDepartureTime());
        existing.setArrivalTime(updatedTrain.getArrivalTime());
        existing.setTotalSeats(updatedTrain.getTotalSeats());
        existing.setAvailableSeats(updatedTrain.getAvailableSeats());

        return trainRepository.save(existing);
    }

    public String deleteTrain(Long id) {
        trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));
        trainRepository.deleteById(id);
        return "Train deleted successfully";
    }

    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    public List<Train> searchTrains(String source, String destination) {
        return trainRepository.findBySourceAndDestination(source, destination);
    }

    public Train updateSeats(Long id, int seats) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));
        train.setAvailableSeats(seats);
        return trainRepository.save(train);
    }

    public Train getTrainById(Long id) {
        return trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Train not found with id: " + id));
    }
}