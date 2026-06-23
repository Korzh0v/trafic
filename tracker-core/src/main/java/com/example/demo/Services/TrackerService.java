package com.example.demo.Services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TrackerService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BusEvent(int number, double lat, double lng, int currentPointIndex) {}

    @KafkaListener(topics = "bus-telemetry", groupId = "tracker-group")
    public void readBusPosition(String message) throws Exception {
        BusEvent bus = objectMapper.readValue(message, BusEvent.class);
        System.out.println(bus);
    }

    @KafkaListener(topics = "transport.gps.raw", groupId = "tracker-group")
    public void saveRouteToDB() {

    }
}