package com.weatherapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenWeatherConfig {

    @Value("${openweathermap.api.key}")
    private String apiKey;

    @Value("${openweathermap.api.geocoding.url}")
    private String geocodingUrl;

    @Value("${openweathermap.api.weather.url}")
    private String weatherUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getGeocodingUrl() {
        return geocodingUrl;
    }

    public String getWeatherUrl() {
        return weatherUrl;
    }

    public String buildGeocodingUrl(String pincode) {
        return geocodingUrl.replace("{pincode}", pincode).replace("{api_key}", apiKey);
    }

    public String buildWeatherUrl(double latitude, double longitude) {
        return weatherUrl + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey + "&units=metric";
    }
}