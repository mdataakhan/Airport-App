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

        // Putting name as a mandatory field
        if (airport.getName() == null || airport.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is a mandatory field and cannot be empty.");
        }

        // Putting Country as a mandatory field
        if (airport.getCountry() == null || !airport.getCountry().matches("^[A-Z]{2}$")) {
            throw new IllegalArgumentException("Country code is a mandatory field and must be two uppercase letters.");
        }

        // Putting Time Zone as a mandatory field
        if (airport.getTz() == null || airport.getTz().trim().isEmpty()) {
            throw new IllegalArgumentException("Timezone is a mandatory field and cannot be empty.");
        }

        // Putting Elevation as a mandatory field
        if (airport.getElevation() == null) {
            throw new IllegalArgumentException("Elevation is a mandatory field and must be an integer.");
        }

        // Setting constraint of latitude and is mandatory
        if (airport.getLat() == null || airport.getLat() < -90.0 || airport.getLat() > 90.0) {
            throw new IllegalArgumentException("Latitude is a mandatory field and must be in the range [-90, +90] degrees.");
        }

        // Setting constraint of longitude and is mandatory
        if (airport.getLon() == null|| airport.getLon() < -180.0 || airport.getLon() > 180.0) {
            throw new IllegalArgumentException("Longitude is a mandatory field and must be in the range [-180, +180] degrees.");
        }

        if (airport.getIata() == null) airport.setIata("");
        if (airport.getCity() == null) airport.setCity("");
        if (airport.getState() == null) airport.setState("");

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