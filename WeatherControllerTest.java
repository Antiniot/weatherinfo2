package com.weatherapi;

import com.weatherapi.controller.WeatherController;
import com.weatherapi.dto.APIResponse;
import com.weatherapi.dto.WeatherResponse;
import com.weatherapi.exception.WeatherApiException;
import com.weatherapi.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherControllerTest {

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private WeatherController weatherController;

    @Test
    void testGetWeatherData_serviceThrowsException() {
        // Arrange
        String pincode = "123456";
        String date = "2024-03-15";

        when(weatherService.getWeatherData(anyString(), any(LocalDate.class)))
                .thenThrow(new WeatherApiException("Service error", 500));

        // Act
        ResponseEntity<APIResponse<WeatherResponse>> responseEntity = weatherController.getWeatherData(pincode, date);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        APIResponse<WeatherResponse> response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
        assertEquals("Error when accessing API: Service error", response.getMessage());
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().containsKey("api"));
        assertEquals("Service error", response.getErrors().get("api"));
    }
}