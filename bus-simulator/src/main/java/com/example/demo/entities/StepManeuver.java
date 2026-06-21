package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StepManeuver(
        List<Double> location,
        Integer bearing_before,
        Integer bearing_after,
        String type,
        String modifier,
        Integer exit
) {
}