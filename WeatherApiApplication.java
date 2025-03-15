// -*- coding: utf-8 -*-
/*
Backend Assignment - Weather Info for Pincode (Java Spring Boot)

This Spring Boot application provides a REST API for retrieving weather information
for a specific pincode and date. It leverages the OpenWeatherMap API for weather
data and a PostgreSQL database to store and optimize API calls.

Key Features:
- RESTful API design (Spring Web MVC)
- Pincode to Latitude/Longitude conversion (using OpenWeather Geocoding API)
- Latitude/Longitude to Weather Information (using OpenWeather API)
- Data Persistence in PostgreSQL (using Spring Data JPA)
- API Call Optimization (caches lat/long and weather data to reduce external API calls)
- Proper Code Structure (using Spring Boot's component-based architecture)
- Testability (using Spring Boot's testing support)

Dependencies:
- Spring Boot Web: For creating the REST API
- Spring Data JPA: For database interaction (ORM)
- PostgreSQL Driver: For connecting to the PostgreSQL database
- Spring Boot Test: For writing test cases
- Lombok: For reducing boilerplate code (optional but recommended)

API Endpoints:
- GET /weather/{pincode}/{date}: Retrieves weather information for the given pincode and date.

Database Schema:
- pincodes: Stores pincode, latitude, and longitude.
- weather_data: Stores weather data for a specific pincode and date.

Configuration:
- Application properties (application.properties or application.yml) are used to
  configure the OpenWeather API key, database connection details, etc.

*/

package com.weatherapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WeatherApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherApiApplication.class, args);
    }

}