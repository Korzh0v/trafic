package com.example.demo;

public record BusAnalyticsSnapshot(
        int    busNumber,
        double avgPassengers,
        int    peakPassengers,
        double lastSpeedKmh,
        double avgSpeedKmh,
        int    totalStops,
        long   totalDwellMs,
        int    trafficCount,
        int    accidentCount,
        long   totalDelaySeconds,
        double lastProgress,
        double lastDistanceToNextStop
) {}