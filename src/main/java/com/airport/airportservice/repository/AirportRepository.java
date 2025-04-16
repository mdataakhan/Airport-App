package com.airport.airportservice.repository;

import com.airport.airportservice.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirportRepository extends JpaRepository<Airport, String> {
    List<Airport> findByNameContainingIgnoreCase(String name);
}
