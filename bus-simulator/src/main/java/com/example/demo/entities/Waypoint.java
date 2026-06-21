package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Waypoint(
        String name,
        Double distance,
        List<Double> location
) {
}