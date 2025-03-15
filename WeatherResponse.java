package com.weatherapi.dto;

import lombok.Data;

@Data
public class WeatherResponse {
    private String pincode;
    private String date;
    private Double temperature;
    private Double humidity;
    private String description;
    private String source; // e.g., "cache" or "api"
}