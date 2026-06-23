package com.example.demo.Services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TrackerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisTemplate<String, String> redisTemplate;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BusEvent(int busNumber,
                           double lat,
                           double lng,
                           double progress,
                           double distanceToNextStop) {
    }

    TrackerService (RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

//    @KafkaListener(topics = "bus-telemetry", groupId = "tracker-group")
//    public void readBusPosition(String message) throws Exception {
//        BusEvent bus = objectMapper.readValue(message, BusEvent.class);
//        System.out.println(bus);
//    }

    @KafkaListener(topics = "transport.gps.raw", groupId = "tracker-group")
    public void saveRouteToDB(String message) throws Exception {
        BusEvent bus = objectMapper.readValue(message, BusEvent.class);
        redisTemplate.opsForValue().set("bus " + bus.busNumber, "lat " + bus.lat + " lng " + bus.lng, Expiration.seconds(10));
        System.out.println("REDIS ID" + bus.busNumber + " lat " + bus.lat + " lng " + bus.lng);
        System.out.println(bus);

    }
}