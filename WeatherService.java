package com.weatherapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.weatherapi.config.OpenWeatherConfig;
import com.weatherapi.dto.WeatherResponse;
import com.weatherapi.exception.WeatherApiException;
import com.weatherapi.model.Pincode;
import com.weatherapi.model.WeatherData;
import com.weatherapi.repository.PincodeRepository;
import com.weatherapi.repository.WeatherDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import java.util.Optional;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    @Autowired
    private OpenWeatherConfig openWeatherConfig;

    @Autowired
    private PincodeRepository pincodeRepository;

    @Autowired
    private WeatherDataRepository weatherDataRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public WeatherResponse getWeatherData(String pincode, LocalDate date) {
        try {
            Optional<WeatherData> existingWeatherData = weatherDataRepository.findByPincodeAndDate(pincode, date);

            if (existingWeatherData.isPresent()) {
                logger.info("Weather data from DB for pincode: {} and date: {}", pincode, date);
                return convertToWeatherResponse(existingWeatherData.get(), "cache");
            }

            // Fetch lat/long and then weather data
            Pincode pincodeData = getLatLongFromPincode(pincode);
            WeatherData weatherData = fetchWeatherDataFromApi(pincodeData.getLatitude(), pincodeData.getLongitude(), pincode, date);
            return convertToWeatherResponse(weatherData, "api");
        } catch (DataAccessException e) {
            logger.error("Database access error while retrieving weather data: {}", e.getMessage(), e);
            throw new WeatherApiException("Error accessing the database. Please try again later.", 500);
        }
    }

    private Pincode getLatLongFromPincode(String pincode) {
        try {
            Optional<Pincode> existingPincode = pincodeRepository.findByPincode(pincode);

            if (existingPincode.isPresent()) {
                logger.info("Lat/Long from DB for pincode: {}", pincode);
                return existingPincode.get();
            }

            // Call OpenWeather Geocoding API
            String geocodingUrl = openWeatherConfig.buildGeocodingUrl(pincode);
            JsonNode data = restTemplate.getForObject(geocodingUrl, JsonNode.class);

            if (data != null && data.isArray() && data.size() > 0) {
                JsonNode firstLocation = data.get(0);
                double latitude = firstLocation.get("lat").asDouble();
                double longitude = firstLocation.get("lon").asDouble();

                Pincode newPincode = new Pincode();
                newPincode.setPincode(pincode);
                newPincode.setLatitude(latitude);
                newPincode.setLongitude(longitude);
                pincodeRepository.save(newPincode);
                logger.info("Lat/Long from OpenWeather API for pincode: {}", pincode);
                return newPincode;
            } else {
                throw new WeatherApiException("Could not find coordinates for pincode: " + pincode, 404);
            }
        } catch (HttpClientErrorException e) {
            logger.error("HTTP client error while fetching geocoding data: {}", e.getMessage(), e);
            throw new WeatherApiException("Invalid pincode provided.", e.getRawStatusCode());
        } catch (ResourceAccessException e) {
            logger.error("Network error while fetching geocoding data: {}", e.getMessage(), e);
            throw new WeatherApiException("Unable to connect to geocoding service. Please check your network connection.", 503);
        } catch (DataAccessException e) {
            logger.error("Database access error while saving pincode data: {}", e.getMessage(), e);
            throw new WeatherApiException("Error accessing the database. Please try again later.", 500);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching geocoding data: {}", e.getMessage(), e);
            throw new WeatherApiException("An unexpected error occurred while fetching geocoding data. Please try again later.", 500);
        }
    }

    private WeatherData fetchWeatherDataFromApi(double latitude, double longitude, String pincode, LocalDate date) {
        String weatherUrl = openWeatherConfig.buildWeatherUrl(latitude, longitude);
        logger.info("Fetching weather data from URL: {}", weatherUrl);

        try {
            logger.debug("Making request to OpenWeather API");
            JsonNode weatherDataFromApi = restTemplate.getForObject(weatherUrl, JsonNode.class);
            logger.debug("Received response from OpenWeather API");

            if (weatherDataFromApi == null) {
                logger.error("Null response received from OpenWeather API");
                throw new WeatherApiException("Weather service is temporarily unavailable. Please try again later.", 503);
            }

            // Log the response for debugging
            logger.debug("OpenWeather API response: {}", weatherDataFromApi.toString());

            // Check for API error response first
            if (weatherDataFromApi.has("cod")) {
                String cod = weatherDataFromApi.get("cod").asText();
                // OpenWeather API can return numeric or string status codes
                if (!"200".equals(cod) && !"200.0".equals(cod)) {
                    String errorMessage = weatherDataFromApi.has("message")
                            ? weatherDataFromApi.get("message").asText()
                            : "Unknown error from weather service";

                    // Parse error code safely
                    int errorCode;
                    try {
                        // Handle both numeric and string representations
                        errorCode = (cod.contains("."))
                                ? (int) Double.parseDouble(cod)
                                : Integer.parseInt(cod);
                    } catch (NumberFormatException e) {
                        logger.warn("Could not parse error code '{}', defaulting to 500", cod);
                        errorCode = 500;
                    }

                    logger.error("OpenWeather API error: {} (code: {})", errorMessage, errorCode);
                    throw new WeatherApiException("OpenWeather API error: " + errorMessage, errorCode);
                }
            }

            // Validate response structure
            if (!weatherDataFromApi.has("main") || !weatherDataFromApi.has("weather") ||
                    !weatherDataFromApi.get("main").has("temp") ||
                    !weatherDataFromApi.get("main").has("humidity") ||
                    !weatherDataFromApi.get("weather").isArray() ||
                    weatherDataFromApi.get("weather").size() == 0) {

                logger.error("Invalid response format from OpenWeather API: {}", weatherDataFromApi);
                throw new WeatherApiException("Unable to process weather data. Please try again later.", 502);
            }

            // Safely extract values with null checks
            double temperature = 0.0;
            double humidity = 0.0;
            String description = "Unknown";

            JsonNode mainNode = weatherDataFromApi.get("main");
            JsonNode weatherArray = weatherDataFromApi.get("weather");

            if (mainNode != null && mainNode.has("temp")) {
                temperature = mainNode.get("temp").asDouble();
            } else {
                logger.warn("Temperature data missing from API response");
            }

            if (mainNode != null && mainNode.has("humidity")) {
                humidity = mainNode.get("humidity").asDouble();
            } else {
                logger.warn("Humidity data missing from API response");
            }

            if (weatherArray != null && weatherArray.isArray() && weatherArray.size() > 0) {
                JsonNode firstWeather = weatherArray.get(0);
                if (firstWeather != null && firstWeather.has("description")) {
                    description = firstWeather.get("description").asText();
                } else {
                    logger.warn("Description missing from API response");
                }
            } else {
                logger.warn("Weather array missing or empty in API response");
            }

            WeatherData newWeatherData = new WeatherData();
            newWeatherData.setPincode(pincode);
            newWeatherData.setDate(date);
            newWeatherData.setTemperature(temperature);
            newWeatherData.setHumidity(humidity);
            newWeatherData.setDescription(description);
            newWeatherData.setCached(true);

            weatherDataRepository.save(newWeatherData);

            logger.info("Weather data fetched from OpenWeather API.");
            return newWeatherData;
        } catch (HttpClientErrorException e) {
            logger.error("HTTP Client error when calling OpenWeather API: {}", e.getMessage(), e);
            throw new WeatherApiException("Invalid request to weather service. Please check your input.", e.getRawStatusCode());
        } catch (ResourceAccessException e) {
            logger.error("Network error when calling OpenWeather API: {}", e.getMessage(), e);
            throw new WeatherApiException("Unable to connect to weather service. Please check your network connection.", 503);
        } catch (DataAccessException e) {
            logger.error("Database access error while saving weather data: {}", e.getMessage(), e);
            throw new WeatherApiException("Error saving weather data. Please try again later.", 500);
        } catch (Exception e) {
            logger.error("Unexpected error fetching weather data: {}", e.getMessage(), e);
            logger.error("Full stack trace:", e); //temporary addition
            throw new WeatherApiException("An unexpected error occurred. Please try again later.", 500);
        }
    }

    private WeatherResponse convertToWeatherResponse(WeatherData weatherData, String source) {
        WeatherResponse response = new WeatherResponse();
        response.setPincode(weatherData.getPincode());
        response.setDate(weatherData.getDate().toString());
        response.setTemperature(weatherData.getTemperature());
        response.setHumidity(weatherData.getHumidity());
        response.setDescription(weatherData.getDescription());
        response.setSource(source);
        return response;
    }
}