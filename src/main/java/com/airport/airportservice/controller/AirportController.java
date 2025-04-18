package com.airport.airportservice.controller;

import com.airport.airportservice.config.DataLoader;
import com.airport.airportservice.model.Airport;
import com.airport.airportservice.service.AirportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/airports")
@CrossOrigin(origins = "*")
public class AirportController {

    private final AirportService airportService;
    private final DataLoader dataLoader;

    @Autowired
    public AirportController(AirportService airportService,DataLoader dataLoader) {
        this.airportService = airportService;
        this.dataLoader = dataLoader;
    }

    // This is for pagination and will be used by UI in order to display data in pages of 10 by default
    @GetMapping("/page")
    public Page<Airport> getPaginatedAirports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        if ("region".equalsIgnoreCase(sortBy)) {
            sortBy = "country";
        }

        return airportService.getAirportsPage(page, size, sortBy);
    }

    // This is for getting all Airport Data
    @GetMapping
    public List<Airport> getAllAirports(@RequestParam(required = false) String sortBy) {
        return airportService.getAllAirports(sortBy);
    }

    //This is for fetching Airport data by ICAO Id
    @GetMapping("/{icao}")
    public ResponseEntity<?> getAirportById(@PathVariable String icao) {
        // Accepting only 4 chars including digits as well without any special character and all char should be upper case
        if (!icao.matches("^[A-Z0-9]{4}$")) {
            throw new IllegalArgumentException("ICAO code must be exactly 4 characters long and contain uppercase letters (A-Z) and digits (0-9). No special characters allowed.");
        }

        // I have taken Optional as there is possibility of null
        Optional<Airport> airport = airportService.getAirportById(icao.toUpperCase());

        // If code format is correct but it does not exist in db it will throw not found
        if (airport.isPresent()) {
            return ResponseEntity.ok(airport.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("errorType", "NotFound");
            error.put("message", "Airport with ICAO code '" + icao + "' was not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    //This is to create an Airport Entry
    @PostMapping
    public Airport addAirport(@RequestBody Airport airport) {
        return airportService.addAirport(airport);
    }

    // This is to delete an entry By ICAO Id
    @DeleteMapping("/{icao}")
    public ResponseEntity<Void> deleteAirport(@PathVariable String icao) {
        airportService.deleteAirport(icao);
        return ResponseEntity.ok().build();
    }

    //This is to filter Output by name field
    @GetMapping("/filter-by-name")
    public ResponseEntity <List<Airport>> filterAirportsByName(@RequestParam String name) {
        List<Airport> filteredlist = airportService.filterByName(name);
        return ResponseEntity.ok(filteredlist);
    }

    //Script 1
    // This is to get the average elevation per country
    @GetMapping("/average-elevation")
    public ResponseEntity<Map<String, Double>> getAverageElevationPerCountry() {
        Map<String, Double> avgElevations = airportService.getAverageElevationPerCountry();
        return ResponseEntity.ok(avgElevations);
    }

    //Script 2
    // This is to get list of data without IATA codes
    @GetMapping("/without-iata")
    public ResponseEntity<List<Airport>> getAirportsWithoutIata() {
        List<Airport> airports = airportService.getAirportsWithoutIataCode();
        return ResponseEntity.ok(airports);
    }

    //Script3
    // This is to get top 10 most common time zones
    @GetMapping("/top-timezones")
    public ResponseEntity<List<Map.Entry<String, Long>>> getTop10TimeZones() {
        List<Map.Entry<String, Long>> topTimeZones = airportService.getTop10TimeZones();
        return ResponseEntity.ok(topTimeZones);
    }

    //This is to load data from user end by taking json as input from User.
    @PostMapping("/load-data")
    public ResponseEntity<String> manuallyLoadAirportData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            dataLoader.loadAirportDataFromInputStream(inputStream);
            return ResponseEntity.ok("Airport data loaded successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading data: " + e.getMessage());
        }
    }
}
