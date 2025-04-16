package com.airport.airportservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airport {

    @Id
   // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String icao;


    private String iata;
    private String name;
    private String city;
    private String state;
    private String country;
    private Integer elevation;
    private Double lat;
    private Double lon;
    private String tz;
    @Transient
    private String region;

    public String getRegion() {
        if (country != null && state != null) {
            return country + "-" + state;
        } else if (country != null) {
            return country;
        } else {
            return "";
        }
    }
}
