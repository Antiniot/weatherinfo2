package com.weatherapi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "weather_data")
@Data // Lombok annotation to generate getters, setters, equals, hashCode, and toString
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "humidity")
    private Double humidity;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "cached", nullable = false)
    private boolean cached = false; // Indicates if data came from cache
}