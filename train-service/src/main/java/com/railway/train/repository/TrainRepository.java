package com.railway.train.repository;

import com.railway.train.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainRepository extends JpaRepository<Train, Long> {

    List<Train> findBySourceAndDestination(String source, String destination);
}