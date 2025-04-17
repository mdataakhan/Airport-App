package com.airport.airportservice.config;

import com.airport.airportservice.model.Airport;
import com.airport.airportservice.repository.AirportRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Component
public class DataLoader {

    private final AirportRepository airportRepository;

    @Autowired
    public DataLoader(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    // Keep auto-load if DB is empty
    @PostConstruct
    private void autoLoadOnStartup() {
        if (airportRepository.count() == 0) {
            loadAirportData();
        }
    }

    public void loadAirportData() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data/airports.json");

            if (inputStream != null) {
                Map<String, Airport> airportMap = objectMapper.readValue(inputStream,
                        new TypeReference<Map<String, Airport>>() {});
                airportRepository.saveAll(airportMap.values());
                System.out.println("Airport data manually loaded.");
            } else {
                System.out.println("airports.json not found in resources.");
            }
        } catch (Exception e) {
            System.out.println("Manual load failed: " + e.getMessage());
        }
    }
}
