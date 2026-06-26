package com.example.demo.Service;

import com.example.demo.DTO.BusEventDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class AlertsService {

    ObjectMapper mapper = new ObjectMapper();

    @JsonIgnoreProperties(ignoreUnknown = true)
    @KafkaListener(topics = { "transport.alerts" })
    public ResponseEntity<BusEventDTO> printEvent(String message) {
        BusEventDTO event = mapper.readValue(message, BusEventDTO.class);
        System.out.println(event);
        System.out.println("1");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            event
        );
    }
}
