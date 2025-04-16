package com.airport.airportservice.service;

import com.airport.airportservice.model.Airport;
import com.airport.airportservice.repository.AirportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
public class AirportService {

    private final AirportRepository airportRepository;

    @Autowired
    public AirportService(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public Page<Airport> getAirportsPage(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return airportRepository.findAll(pageable);
    }

    public List<Airport> getAllAirports() {
        List<Airport> airports = airportRepository.findAll();
        airports.forEach(this::populateDerivedFields);
        return airports;
    }

    private void populateDerivedFields(Airport airport) {
        airport.setRegion(airport.getRegion()); // triggers the getter
    }

    public List<Airport> getAllAirports(String sortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            return airportRepository.findAll();
        }
        return airportRepository.findAll(Sort.by(sortBy));
    }

    public List<Airport> filterByName(String name) {
        return airportRepository.findByNameContainingIgnoreCase(name);
    }

    public Optional<Airport> getAirportById(String icao) {
        return airportRepository.findById(icao);
    }

    public Airport addAirport(Airport airport) {
        return airportRepository.save(airport);
    }

    public void deleteAirport(String icao) {
        airportRepository.deleteById(icao);
    }
}
