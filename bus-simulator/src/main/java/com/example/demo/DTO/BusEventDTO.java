package com.example.demo.DTO;

public record BusEventDTO(
    int busNumber,
    String eventType, // "TRAFFIC" або "ACCIDENT"
    double lat,
    double lng,
    int durationTicks,
    long timestamp
) {}
