package com.airport.airportservice.service;

import com.airport.airportservice.model.Airport;
import com.airport.airportservice.repository.AirportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirportServiceTest {

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private AirportService airportService;

    private List<Airport> mockAirports;

    @BeforeEach
    void setUp() {
        // Initialize mock airport data
        mockAirports = new ArrayList<>();
        Airport airport1 = new Airport();
        airport1.setIcao("KJFK");
        airport1.setIata("JFK");
        airport1.setName("John F Kennedy International");
        airport1.setCity("New York");
        airport1.setState("NY");
        airport1.setCountry("US");
        airport1.setElevation(13);
        airport1.setLat(40.6398);
        airport1.setLon(-73.7789);
        airport1.setTz("America/New_York");
        airport1.setRegion("North America");

        Airport airport2 = new Airport();
        airport2.setIcao("EGLL");
        airport2.setIata("");
        airport2.setName("Heathrow Airport");
        airport2.setCity("London");
        airport2.setState("");
        airport2.setCountry("GB");
        airport2.setElevation(83);
        airport2.setLat(51.4706);
        airport2.setLon(-0.4619);
        airport2.setTz("Europe/London");
        airport2.setRegion("Europe");

        mockAirports.add(airport1);
        mockAirports.add(airport2);
    }

    @Test
    void getAirportsPage_ShouldReturnPagedAirports() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "name";
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Airport> mockPage = new PageImpl<>(mockAirports, pageable, mockAirports.size());

        when(airportRepository.findAll(pageable)).thenReturn(mockPage);

        // Act
        Page<Airport> result = airportService.getAirportsPage(page, size, sortBy);

        // Assert
        assertEquals(2, result.getContent().size());
        assertEquals(mockAirports, result.getContent());
        verify(airportRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAllAirports_WithoutSort_ShouldReturnAllAirports() {
        // Arrange
        when(airportRepository.findAll()).thenReturn(mockAirports);

        // Act
        List<Airport> result = airportService.getAllAirports(null);

        // Assert
        assertEquals(2, result.size());
        assertEquals(mockAirports, result);
        verify(airportRepository, times(1)).findAll();
    }

    @Test
    void getAllAirports_WithValidSort_ShouldReturnSortedAirports() {
        // Arrange
        String sortBy = "name";
        when(airportRepository.findAll(Sort.by(sortBy))).thenReturn(mockAirports);

        // Act
        List<Airport> result = airportService.getAllAirports(sortBy);

        // Assert
        assertEquals(2, result.size());
        assertEquals(mockAirports, result);
        verify(airportRepository, times(1)).findAll(Sort.by(sortBy));
    }

    @Test
    void getAllAirports_WithInvalidSort_ShouldThrowException() {
        // Arrange
        String sortBy = "invalidField";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.getAllAirports(sortBy)
        );
        assertEquals("Sorting by 'invalidField' is not allowed. Allowed fields: name, city, state, country.", exception.getMessage());
        verify(airportRepository, never()).findAll(any(Sort.class));
    }

    @Test
    void filterByName_ShouldReturnFilteredAirports() {
        // Arrange
        String name = "Kennedy";
        List<Airport> filteredList = mockAirports.subList(0, 1); // Only KJFK
        when(airportRepository.findByNameContainingIgnoreCase(name)).thenReturn(filteredList);

        // Act
        List<Airport> result = airportService.filterByName(name);

        // Assert
        assertEquals(1, result.size());
        assertEquals("KJFK", result.get(0).getIcao());
        verify(airportRepository, times(1)).findByNameContainingIgnoreCase(name);
    }

    @Test
    void getAirportById_WhenExists_ShouldReturnAirport() {
        // Arrange
        String icao = "KJFK";
        Optional<Airport> mockAirport = Optional.of(mockAirports.get(0));
        when(airportRepository.findById(icao)).thenReturn(mockAirport);

        // Act
        Optional<Airport> result = airportService.getAirportById(icao);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("KJFK", result.get().getIcao());
        verify(airportRepository, times(1)).findById(icao);
    }

    @Test
    void getAirportById_WhenNotExists_ShouldReturnEmpty() {
        // Arrange
        String icao = "XXXX";
        when(airportRepository.findById(icao)).thenReturn(Optional.empty());

        // Act
        Optional<Airport> result = airportService.getAirportById(icao);

        // Assert
        assertFalse(result.isPresent());
        verify(airportRepository, times(1)).findById(icao);
    }

    @Test
    void addAirport_ValidAirport_ShouldSaveAirport() {
        // Arrange
        Airport newAirport = new Airport();
        newAirport.setIcao("KLAX");
        newAirport.setIata("");
        newAirport.setName("Los Angeles International");
        newAirport.setCity("");
        newAirport.setState("");
        newAirport.setCountry("US");
        newAirport.setElevation(125);
        newAirport.setLat(33.9425);
        newAirport.setLon(-118.4081);
        newAirport.setTz("America/Los_Angeles");

        when(airportRepository.existsById("KLAX")).thenReturn(false);
        when(airportRepository.save(any(Airport.class))).thenReturn(newAirport);

        // Act
        Airport result = airportService.addAirport(newAirport);

        // Assert
        assertEquals("KLAX", result.getIcao());
        assertEquals("", result.getIata()); // Defaulted to ""
        assertEquals("", result.getCity()); // Defaulted to ""
        assertEquals("", result.getState()); // Defaulted to ""
        verify(airportRepository, times(1)).existsById("KLAX");
        verify(airportRepository, times(1)).save(newAirport);
    }

    @Test
    void addAirport_ValidAirportWithIntegerLatLon_ShouldSaveAirport() {
        // Arrange
        Airport newAirport = new Airport();
        newAirport.setIcao("KSEA");
        newAirport.setIata("SEA");
        newAirport.setName("Seattle-Tacoma International");
        newAirport.setCity("Seattle");
        newAirport.setState("WA");
        newAirport.setCountry("US");
        newAirport.setElevation(433);
        newAirport.setLat(47.0); // Integer input
        newAirport.setLon(-122.0); // Integer input
        newAirport.setTz("America/Los_Angeles");

        when(airportRepository.existsById("KSEA")).thenReturn(false);
        when(airportRepository.save(any(Airport.class))).thenReturn(newAirport);

        // Act
        Airport result = airportService.addAirport(newAirport);

        // Assert
        assertEquals("KSEA", result.getIcao());
        assertEquals(47.0, result.getLat());
        assertEquals(-122.0, result.getLon());
        verify(airportRepository, times(1)).existsById("KSEA");
        verify(airportRepository, times(1)).save(newAirport);
    }

    @Test
    void addAirport_MissingIcao_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao(null);
        airport.setName("Test Airport");
        airport.setCountry("US");
        airport.setTz("America/Los_Angeles");
        airport.setElevation(125);
        airport.setLat(33.9425);
        airport.setLon(-118.4081);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("ICAO code is a mandatory field, which should not contain special or lowercase characters and must be exactly 4 characters.", exception.getMessage());
        verify(airportRepository, never()).existsById(anyString());
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_InvalidIcao_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao("abc"); // Invalid ICAO (3 chars)
        airport.setName("Test Airport");
        airport.setCountry("US");
        airport.setTz("America/Los_Angeles");
        airport.setElevation(125);
        airport.setLat(33.9425);
        airport.setLon(-118.4081);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("ICAO code is a mandatory field, which should not contain special or lowercase characters and must be exactly 4 characters.", exception.getMessage());
        verify(airportRepository, never()).existsById(anyString());
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_DuplicateIcao_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao("KJFK");
        airport.setName("Test Airport");
        airport.setCountry("US");
        airport.setTz("America/Los_Angeles");
        airport.setElevation(125);
        airport.setLat(33.9425);
        airport.setLon(-118.4081);
        when(airportRepository.existsById("KJFK")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("Airport with ICAO code 'KJFK' already exists.", exception.getMessage());
        verify(airportRepository, times(1)).existsById("KJFK");
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_MissingName_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao("KLAX");
        airport.setName(null);
        airport.setCountry("US");
        airport.setTz("America/Los_Angeles");
        airport.setElevation(125);
        airport.setLat(33.9425);
        airport.setLon(-118.4081);
        when(airportRepository.existsById("KLAX")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("Name is a mandatory field and cannot be empty.", exception.getMessage());
        verify(airportRepository, times(1)).existsById("KLAX");
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_EmptyName_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao("KLAX");
        airport.setName("");
        airport.setCountry("US");
        airport.setTz("America/Los_Angeles");
        airport.setElevation(125);
        airport.setLat(33.9425);
        airport.setLon(-118.4081);
        when(airportRepository.existsById("KLAX")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("Name is a mandatory field and cannot be empty.", exception.getMessage());
        verify(airportRepository, times(1)).existsById("KLAX");
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_MissingCountry_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao("KLAX");
        airport.setName("Test Airport");
        airport.setCountry(null);
        airport.setTz("America/Los_Angeles");
        airport.setElevation(125);
        airport.setLat(33.9425);
        airport.setLon(-118.4081);
        when(airportRepository.existsById("KLAX")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("Country code is a mandatory field and must be two uppercase letters.", exception.getMessage());
        verify(airportRepository, times(1)).existsById("KLAX");
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_InvalidCountry_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao("KLAX");
        airport.setName("Test Airport");
        airport.setCountry("USA");
        airport.setTz("America/Los_Angeles");
        airport.setElevation(125);
        airport.setLat(33.9425);
        airport.setLon(-118.4081);
        when(airportRepository.existsById("KLAX")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("Country code is a mandatory field and must be two uppercase letters.", exception.getMessage());
        verify(airportRepository, times(1)).existsById("KLAX");
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_MissingTimezone_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao("KLAX");
        airport.setName("Test Airport");
        airport.setCountry("US");
        airport.setTz(null);
        airport.setElevation(125);
        airport.setLat(33.9425);
        airport.setLon(-118.4081);
        when(airportRepository.existsById("KLAX")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("Timezone is a mandatory field and cannot be empty.", exception.getMessage());
        verify(airportRepository, times(1)).existsById("KLAX");
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_EmptyTimezone_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao("KLAX");
        airport.setName("Test Airport");
        airport.setCountry("US");
        airport.setTz("");
        airport.setElevation(125);
        airport.setLat(33.9425);
        airport.setLon(-118.4081);
        when(airportRepository.existsById("KLAX")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("Timezone is a mandatory field and cannot be empty.", exception.getMessage());
        verify(airportRepository, times(1)).existsById("KLAX");
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_MissingElevation_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao("KLAX");
        airport.setName("Test Airport");
        airport.setCountry("US");
        airport.setTz("America/Los_Angeles");
        airport.setElevation(null);
        airport.setLat(33.9425);
        airport.setLon(-118.4081);
        when(airportRepository.existsById("KLAX")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("Elevation is a mandatory field and must be an integer.", exception.getMessage());
        verify(airportRepository, times(1)).existsById("KLAX");
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_MissingLatitude_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao("KLAX");
        airport.setName("Test Airport");
        airport.setCountry("US");
        airport.setTz("America/Los_Angeles");
        airport.setElevation(125);
        airport.setLat(null);
        airport.setLon(-118.4081);
        when(airportRepository.existsById("KLAX")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("Latitude is a mandatory field and must be in the range [-90, +90] degrees.", exception.getMessage());
        verify(airportRepository, times(1)).existsById("KLAX");
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_InvalidLatitude_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao("KLAX");
        airport.setName("Test Airport");
        airport.setCountry("US");
        airport.setTz("America/Los_Angeles");
        airport.setElevation(125);
        airport.setLat(91.0);
        airport.setLon(-118.4081);
        when(airportRepository.existsById("KLAX")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("Latitude is a mandatory field and must be in the range [-90, +90] degrees.", exception.getMessage());
        verify(airportRepository, times(1)).existsById("KLAX");
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_MissingLongitude_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao("KLAX");
        airport.setName("Test Airport");
        airport.setCountry("US");
        airport.setTz("America/Los_Angeles");
        airport.setElevation(125);
        airport.setLat(33.9425);
        airport.setLon(null);
        when(airportRepository.existsById("KLAX")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("Longitude is a mandatory field and must be in the range [-180, +180] degrees.", exception.getMessage());
        verify(airportRepository, times(1)).existsById("KLAX");
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_InvalidLongitude_ShouldThrowException() {
        // Arrange
        Airport airport = new Airport();
        airport.setIcao("KLAX");
        airport.setName("Test Airport");
        airport.setCountry("US");
        airport.setTz("America/Los_Angeles");
        airport.setElevation(125);
        airport.setLat(33.9425);
        airport.setLon(181.0);
        when(airportRepository.existsById("KLAX")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.addAirport(airport)
        );
        assertEquals("Longitude is a mandatory field and must be in the range [-180, +180] degrees.", exception.getMessage());
        verify(airportRepository, times(1)).existsById("KLAX");
        verify(airportRepository, never()).save(any(Airport.class));
    }

    @Test
    void addAirport_NullOptionalFields_ShouldSetDefaults() {
        // Arrange
        Airport newAirport = new Airport();
        newAirport.setIcao("KLAX");
        newAirport.setName("Los Angeles International");
        newAirport.setCountry("US");
        newAirport.setTz("America/Los_Angeles");
        newAirport.setElevation(125);
        newAirport.setLat(33.9425);
        newAirport.setLon(-118.4081);
        // iata, city, state are null

        Airport savedAirport = new Airport();
        savedAirport.setIcao("KLAX");
        savedAirport.setName("Los Angeles International");
        savedAirport.setCountry("US");
        savedAirport.setTz("America/Los_Angeles");
        savedAirport.setElevation(125);
        savedAirport.setLat(33.9425);
        savedAirport.setLon(-118.4081);
        savedAirport.setIata("");
        savedAirport.setCity("");
        savedAirport.setState("");

        when(airportRepository.existsById("KLAX")).thenReturn(false);
        when(airportRepository.save(any(Airport.class))).thenReturn(savedAirport);

        // Act
        Airport result = airportService.addAirport(newAirport);

        // Assert
        assertEquals("KLAX", result.getIcao());
        assertEquals("", result.getIata());
        assertEquals("", result.getCity());
        assertEquals("", result.getState());
        verify(airportRepository, times(1)).existsById("KLAX");
        verify(airportRepository, times(1)).save(any(Airport.class));
    }

    @Test
    void deleteAirport_WhenExists_ShouldDelete() {
        // Arrange
        String icao = "KJFK";
        when(airportRepository.existsById(icao)).thenReturn(true);

        // Act
        airportService.deleteAirport(icao);

        // Assert
        verify(airportRepository, times(1)).existsById(icao);
        verify(airportRepository, times(1)).deleteById(icao);
    }

    @Test
    void deleteAirport_WhenNotExists_ShouldThrowException() {
        // Arrange
        String icao = "XXXX";
        when(airportRepository.existsById(icao)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> airportService.deleteAirport(icao)
        );
        assertEquals("No Data found associated with given ICAO: XXXX", exception.getMessage());
        verify(airportRepository, times(1)).existsById(icao);
        verify(airportRepository, never()).deleteById(icao);
    }

    @Test
    void getAverageElevationPerCountry_ShouldReturnAverages() {
        // Arrange
        when(airportRepository.findAll()).thenReturn(mockAirports);

        // Act
        Map<String, Double> result = airportService.getAverageElevationPerCountry();

        // Assert
        assertEquals(2, result.size());
        assertEquals(13.0, result.get("US"));
        assertEquals(83.0, result.get("GB"));
        verify(airportRepository, times(1)).findAll();
    }

    @Test
    void getAverageElevationPerCountry_WithEmptyCountry_ShouldExclude() {
        // Arrange
        Airport airportWithEmptyCountry = new Airport();
        airportWithEmptyCountry.setIcao("XXXX");
        airportWithEmptyCountry.setName("Test Airport");
        airportWithEmptyCountry.setCountry("");
        airportWithEmptyCountry.setElevation(100);
        mockAirports.add(airportWithEmptyCountry);

        when(airportRepository.findAll()).thenReturn(mockAirports);

        // Act
        Map<String, Double> result = airportService.getAverageElevationPerCountry();

        // Assert
        assertEquals(2, result.size());
        assertFalse(result.containsKey(""));
        assertEquals(13.0, result.get("US"));
        assertEquals(83.0, result.get("GB"));
        verify(airportRepository, times(1)).findAll();
    }

    @Test
    void getAirportsWithoutIataCode_ShouldReturnAirportsWithoutIata() {
        // Arrange
        when(airportRepository.findAll()).thenReturn(mockAirports);

        // Act
        List<Airport> result = airportService.getAirportsWithoutIataCode();

        // Assert
        assertEquals(1, result.size());
        assertEquals("EGLL", result.get(0).getIcao());
        verify(airportRepository, times(1)).findAll();
    }
}