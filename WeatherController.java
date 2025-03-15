package com.weatherapi.controller;

import com.weatherapi.dto.APIResponse;
import com.weatherapi.dto.WeatherResponse;
import com.weatherapi.exception.WeatherApiException;
import com.weatherapi.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/{pincode}/{date}")
    public ResponseEntity<APIResponse<WeatherResponse>> getWeatherData(@PathVariable String pincode, @PathVariable String date) {
        try {
            LocalDate dateObj = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            WeatherResponse weatherData = weatherService.getWeatherData(pincode, dateObj);

            APIResponse<WeatherResponse> response = new APIResponse<>();
            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Weather data retrieved successfully");
            response.setData(weatherData);

            return ResponseEntity.ok(response);

        } catch (DateTimeParseException e) {
            APIResponse<WeatherResponse> response = new APIResponse<>();
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Invalid date format");
            response.getErrors().put("date", "Invalid date format. Use YYYY-MM-DD.");

            return ResponseEntity.badRequest().body(response);
        } catch (WeatherApiException e) {
            APIResponse<WeatherResponse> response = new APIResponse<>();
            response.setStatusCode(e.getStatusCode() != 0 ? e.getStatusCode() : HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setMessage("Error when accessing API: " + e.getMessage());
            response.getErrors().put("api", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}