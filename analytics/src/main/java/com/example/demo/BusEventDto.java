package com.example.demo;

public record BusEventDto(
        int busNumber,
        String eventType,
        double lat,
        double lng,
        int passengers,
        int durationTicks,
        long timestamp) {}