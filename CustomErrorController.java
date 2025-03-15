package com.weatherapi.exception;

import com.weatherapi.dto.APIResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<APIResponse<Void>> handleError(HttpServletRequest request) {
        String originalUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        APIResponse<Void> response = new APIResponse<>();
        response.setStatusCode(HttpStatus.NOT_FOUND.value());
        HashMap<String, String> errors = new HashMap<>();

        if (originalUri != null && originalUri.matches("/weather/\\d+/\\d{4}-\\d{2}-\\d{2}")) {
            response.setMessage("Invalid API path");
            errors.put("path", "Please use '/api/weather/' instead of '/weather/' for weather data requests.");
        } else {
            response.setMessage("Resource not found");
            errors.put("path", "Please check the URL and try again.");
        }
        
        response.setErrors(errors);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}