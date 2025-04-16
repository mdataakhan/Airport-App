package com.airport.airportservice.controller;

import com.airport.airportservice.config.DataLoader;
import com.airport.airportservice.model.Airport;
import com.airport.airportservice.service.AirportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/airports")
@CrossOrigin(origins = "*") // Allows access from any frontend (like React)
public class AirportController {

    private final AirportService airportService;
    private final DataLoader dataLoader;

    @Autowired
    public AirportController(AirportService airportService,DataLoader dataLoader) {
        this.airportService = airportService;
        this.dataLoader = dataLoader;
    }

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

    @PostMapping("/load-data")
    public String manuallyLoadAirportData() {
        dataLoader.loadAirportData();
        return "Airport data load triggered.";
    }

    @GetMapping
    public List<Airport> getAllAirports(@RequestParam(required = false) String sortBy) {
        return airportService.getAllAirports(sortBy);
    }

    @GetMapping("/{icao}")
    public ResponseEntity<?> getAirportById(@PathVariable String icao) {
        // Validate ICAO code: exactly 4 uppercase letters, no digits/special chars
        if (!icao.matches("^[A-Z]{4}$")) {
            Map<String, String> error = new HashMap<>();
            error.put("errorType", "InvalidICAOCode");
            error.put("message", "ICAO code must be exactly 4 uppercase letters (A-Z) with no digits or special characters.");
            return ResponseEntity.badRequest().body(error);
        }

        Optional<Airport> airport = airportService.getAirportById(icao);

        if (airport.isPresent()) {
            return ResponseEntity.ok(airport.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("errorType", "NotFound");
            error.put("message", "Airport with ICAO code '" + icao + "' was not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }


    @PostMapping
    public Airport addAirport(@RequestBody Airport airport) {
        return airportService.addAirport(airport);
    }

    @DeleteMapping("/{icao}")
    public void deleteAirport(@PathVariable String icao) {
        airportService.deleteAirport(icao);
    }

    @GetMapping("/filter-by-name")
    public List<Airport> filterAirportsByName(@RequestParam String name) {
        return airportService.filterByName(name);
    }
}
