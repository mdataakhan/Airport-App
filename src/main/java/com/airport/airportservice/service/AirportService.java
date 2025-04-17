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

        List<String> allowedFields = List.of("name", "city", "state", "country");
        if (!allowedFields.contains(sortBy)) {
            throw new IllegalArgumentException("Sorting by '" + sortBy + "' is not allowed. Allowed fields: name, city, state, country.");
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
        String icao = airport.getIcao();

        // Validate ICAO
        if (icao == null || !icao.matches("^[A-Z0-9]{4}$")) {
            throw new IllegalArgumentException("ICAO code is a mandatory field, which should not contain special or lowercase characters and must be exactly 4 characters.");
        }

        // Check if ICAO already exists
        if (airportRepository.existsById(icao)) {
            throw new IllegalArgumentException("Airport with ICAO code '" + icao + "' already exists.");
        }

        // Set default values if fields are null
        if (airport.getIata() == null) airport.setIata("");
        if (airport.getName() == null) airport.setName("");
        if (airport.getCity() == null) airport.setCity("");
        if (airport.getState() == null) airport.setState("");
        if (airport.getCountry() == null) airport.setCountry("");
        if (airport.getTz() == null) airport.setTz("");
        if (airport.getElevation() == null) airport.setElevation(0);
        if (airport.getLat() == null) airport.setLat(0.0);
        if (airport.getLon() == null) airport.setLon(0.0);

        // Validate latitude
        if (airport.getLat() < -90.0 || airport.getLat() > 90.0) {
            throw new IllegalArgumentException("Latitude must be in the range [-90, +90].");
        }

        // Validate longitude
        if (airport.getLon() < -180.0 || airport.getLon() > 180.0) {
            throw new IllegalArgumentException("Longitude must be in the range [-180, +180].");
        }

        // Validate elevation
        if (airport.getElevation() < 0) {
            throw new IllegalArgumentException("Elevation cannot be negative.");
        }

        return airportRepository.save(airport);
    }




    public void deleteAirport(String icao) {
        airportRepository.deleteById(icao);
    }
}
