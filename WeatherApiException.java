package com.weatherapi.exception;

public class WeatherApiException extends RuntimeException {
    private int statusCode;

    public WeatherApiException(String message) {
        super(message);
        this.statusCode = 500;
    }

    public WeatherApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public WeatherApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }

    public int getStatusCode() {
        return statusCode;
    }
}