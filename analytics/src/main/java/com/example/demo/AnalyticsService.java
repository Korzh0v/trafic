package com.example.demo;

import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class AnalyticsService {

    private static final ObjectMapper mapper = new ObjectMapper();

    public void process(String key, String value) {
        try {
            JsonNode node = mapper.readTree(value);

            if (node.has("eventType")) {
                BusEventDto event = mapper.treeToValue(node, BusEventDto.class);
                System.out.println("Key: " + key);
                System.out.println("Event | Bus: " + event.busNumber() + " | Type: " + event.eventType() + " | Passengers: " + event.passengers());
            } else {
                BusDTO bus = mapper.treeToValue(node, BusDTO.class);
                System.out.println("Key: " + key);
                System.out.println("Telemetry | Bus: " + bus.getBusNumber() + " | Lat: " + bus.getLat() + " | Lng: " + bus.getLng() + " | Passengers: " + bus.getPassengers());
            }

        } catch (Exception e) {
            System.err.println("Failed to deserialize. Key: " + key + " | Error: " + e.getMessage());
        }
    }
}