package com.weatherapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.weatherapi.config.OpenWeatherConfig;
import com.weatherapi.dto.WeatherResponse;
import com.weatherapi.exception.WeatherApiException;
import com.weatherapi.model.Pincode;
import com.weatherapi.model.WeatherData;
import com.weatherapi.repository.PincodeRepository;
import com.weatherapi.repository.WeatherDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(WeatherServiceTest.class);

    @Mock
    private OpenWeatherConfig openWeatherConfig;

    @Mock
    private PincodeRepository pincodeRepository;

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WeatherService weatherService;

    @BeforeEach
    void setup() {
    }


    @Test
    void testGetWeatherData_fromCache() {
        // Arrange
        String pincode = "123456";
        LocalDate date = LocalDate.now();
        WeatherData weatherData = new WeatherData();
        weatherData.setPincode(pincode);
        weatherData.setDate(date);
        weatherData.setTemperature(25.0);
        weatherData.setDescription("Sunny");

        when(weatherDataRepository.findByPincodeAndDate(pincode, date)).thenReturn(Optional.of(weatherData));

        // Act
        WeatherResponse response = weatherService.getWeatherData(pincode, date);

        // Assert
        assertEquals(pincode, response.getPincode());
        assertEquals("cache", response.getSource());
    }

    @Test
    void testGetWeatherData_apiSuccess() {
        // Arrange
        String pincode = "654321";
        LocalDate date = LocalDate.now();
        Pincode pincodeData = new Pincode();
        pincodeData.setPincode(pincode);
        pincodeData.setLatitude(40.0);
        pincodeData.setLongitude(-74.0);

        when(pincodeRepository.findByPincode(pincode)).thenReturn(Optional.of(pincodeData));
        when(openWeatherConfig.buildWeatherUrl(40.0, -74.0)).thenReturn("http://example.com/weather");
        when(restTemplate.getForObject("http://example.com/weather", JsonNode.class)).thenReturn(createMockWeatherJson());

        // Act
        WeatherResponse response = weatherService.getWeatherData(pincode, date);

        // Assert
        assertEquals(pincode, response.getPincode());
        assertEquals("api", response.getSource());
    }

    @Test
    void testGetWeatherData_geocodingApiFailure() {
        String pincode = "987654";
        LocalDate date = LocalDate.now();

        when(pincodeRepository.findByPincode(pincode)).thenReturn(Optional.empty());
        when(openWeatherConfig.buildGeocodingUrl(pincode)).thenReturn("http://example.com/geocoding");
        when(restTemplate.getForObject("http://example.com/geocoding", JsonNode.class)).thenReturn(null);

        // Act and Assert
        assertThrows(WeatherApiException.class, () -> weatherService.getWeatherData(pincode, date));
    }

    @Test
    void testGetWeatherData_weatherApiFailure() {
        // Arrange
        String pincode = "567890";
        LocalDate date = LocalDate.now();
        Pincode pincodeData = new Pincode();
        pincodeData.setPincode(pincode);
        pincodeData.setLatitude(34.0);
        pincodeData.setLongitude(-118.0);

        when(pincodeRepository.findByPincode(pincode)).thenReturn(Optional.of(pincodeData));
        when(openWeatherConfig.buildWeatherUrl(34.0, -118.0)).thenReturn("http://example.com/weather");
        when(restTemplate.getForObject("http://example.com/weather", JsonNode.class)).thenThrow(new HttpClientErrorException(org.springframework.http.HttpStatus.BAD_REQUEST));

        // Act and Assert
        assertThrows(WeatherApiException.class, () -> weatherService.getWeatherData(pincode, date));
    }

    private JsonNode createMockWeatherJson() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        ObjectNode mainNode = root.putObject("main");
        mainNode.put("temp", 28.0);
        mainNode.put("humidity", 70);

        ArrayNode weatherArray = root.putArray("weather");
        ObjectNode weatherNode = weatherArray.addObject();
        weatherNode.put("description", "Clear sky");
        logger.debug("Mock Weather JSON: {}", root.toString());

        return root;
    }
}