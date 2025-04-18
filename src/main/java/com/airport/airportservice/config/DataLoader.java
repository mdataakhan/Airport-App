package com.airport.airportservice.config;

import com.airport.airportservice.model.Airport;
import com.airport.airportservice.repository.AirportRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    // This will take the stream from user provided json and will populate the db.
    public void loadAirportDataFromInputStream(InputStream inputStream) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Airport> airportMap = objectMapper.readValue(inputStream,
                    new TypeReference<Map<String, Airport>>() {});
            airportRepository.saveAll(airportMap.values());
            System.out.println("Airport data loaded from uploaded file.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load airport data: " + e.getMessage(), e);
        }
    }
}
