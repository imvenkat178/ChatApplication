package com.cabapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class GeocodingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // OpenStreetMap Nominatim API - FREE (no API key needed)
    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org";

    public GeocodingService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Convert address to coordinates using OpenStreetMap Nominatim
    public Coordinates getCoordinatesFromAddress(String address) {
        try {
            String url = String.format(
                    "%s/search?q=%s&format=json&limit=1&addressdetails=1",
                    NOMINATIM_BASE_URL,
                    address.replace(" ", "%20")
            );

            // Add User-Agent header (required by Nominatim)
            var headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "CabBookingApp/1.0");

            var entity = new org.springframework.http.HttpEntity<>(headers);
            var response = restTemplate.exchange(url,
                    org.springframework.http.HttpMethod.GET, entity, String.class);

            JsonNode jsonArray = objectMapper.readTree(response.getBody());

            if (jsonArray.isArray() && jsonArray.size() > 0) {
                JsonNode location = jsonArray.get(0);

                double lat = location.get("lat").asDouble();
                double lng = location.get("lon").asDouble();

                log.info("Geocoded address '{}' to coordinates: {}, {}", address, lat, lng);
                return new Coordinates(lat, lng);
            }

            log.error("No coordinates found for address: {}", address);
            return null;

        } catch (Exception e) {
            log.error("Error geocoding address: {}", address, e);
            return null;
        }
    }

    // Convert coordinates to address using OpenStreetMap Nominatim (reverse geocoding)
    public String getAddressFromCoordinates(double latitude, double longitude) {
        try {
            String url = String.format(
                    "%s/reverse?lat=%f&lon=%f&format=json&addressdetails=1",
                    NOMINATIM_BASE_URL, latitude, longitude
            );

            // Add User-Agent header (required by Nominatim)
            var headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "CabBookingApp/1.0");

            var entity = new org.springframework.http.HttpEntity<>(headers);
            var response = restTemplate.exchange(url,
                    org.springframework.http.HttpMethod.GET, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if (jsonNode.has("display_name")) {
                String address = jsonNode.get("display_name").asText();
                log.info("Reverse geocoded coordinates {}, {} to address: {}",
                        latitude, longitude, address);
                return address;
            }

            return "Unknown Address";

        } catch (Exception e) {
            log.error("Error in reverse geocoding: {}, {}", latitude, longitude, e);
            return "Address not found";
        }
    }

    // Helper class for coordinates
    public static class Coordinates {
        public final double latitude;
        public final double longitude;

        public Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}