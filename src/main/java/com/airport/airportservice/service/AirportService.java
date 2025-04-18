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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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

    // This is for sorting functionality and only allows for field name / city / state / country
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

    //This is to filter search by given name can be substring of name
    public List<Airport> filterByName(String name) {
        return airportRepository.findByNameContainingIgnoreCase(name);
    }

    //This is to get airport by ICAO
    public Optional<Airport> getAirportById(String icao) {
        return airportRepository.findById(icao);
    }

    //This is to add Airport details with param handling
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
        if (airport.getCountry() == null) {
            airport.setCountry("");
        } else if (!airport.getCountry().matches("^[A-Z]{2}$")) {
            throw new IllegalArgumentException("Country code should be two letters in uppercase.");
        }
        if (airport.getTz() == null) airport.setTz("");
        if (airport.getElevation() == null) airport.setElevation(0);
        if (airport.getLat() == null) airport.setLat(0.0);
        if (airport.getLon() == null) airport.setLon(0.0);

        // This is to ensure latitude constraints
        if (airport.getLat() < -90.0 || airport.getLat() > 90.0) {
            throw new IllegalArgumentException("Latitude must be in the range [-90, +90].");
        }

        // This is to ensure longitude constraints
        if (airport.getLon() < -180.0 || airport.getLon() > 180.0) {
            throw new IllegalArgumentException("Longitude must be in the range [-180, +180].");
        }

        return airportRepository.save(airport);
    }

    //This is to delete Airport Entry By Icao if lets say it doesnot exist will throw an error
    public void deleteAirport(String icao) {
        if(airportRepository.existsById(icao)) {
            airportRepository.deleteById(icao);
        }else {
            throw new IllegalArgumentException("No Data found associated with given ICAO: " + icao);
        }
    }

    // To find average elevation on entire data
    public Map<String, Double> getAverageElevationPerCountry() {
        List<Airport> airports = airportRepository.findAll();

        return airports.stream()
                .filter(a -> a.getCountry() != null && !a.getCountry().isEmpty())
                .collect(Collectors.groupingBy(
                        Airport::getCountry,
                        Collectors.averagingInt(Airport::getElevation)
                ));
    }

    //To get List of Airports without IATO code
    public List<Airport> getAirportsWithoutIataCode() {
        return airportRepository.findAll().stream()
                .filter(a -> a.getIata() == null || a.getIata().isBlank())
                .collect(Collectors.toList());
    }

    //This is to find 10 most common Time Zones
    public List<Map.Entry<String, Long>> getTop10TimeZones() {
        return airportRepository.findAll().stream()
                .filter(a -> a.getTz() != null && !a.getTz().isBlank())
                .collect(Collectors.groupingBy(Airport::getTz, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());
    }


}
