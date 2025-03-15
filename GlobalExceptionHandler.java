package com.weatherapi.exception;

import com.weatherapi.dto.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(WeatherApiException.class)
    public ResponseEntity<APIResponse<Object>> handleWeatherApiException(WeatherApiException ex) {
        APIResponse<Object> response = new APIResponse<>();
        response.setStatusCode(ex.getStatusCode());
        response.setMessage("Error fetching weather data");
        HashMap<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        response.setErrors(errors);
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(Exception.class)  // Catch-all for other exceptions
    public ResponseEntity<APIResponse<Object>> handleGenericException(Exception ex) {
        APIResponse<Object> response = new APIResponse<>();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage("An unexpected error occurred");
        HashMap<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        response.setErrors(errors);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}