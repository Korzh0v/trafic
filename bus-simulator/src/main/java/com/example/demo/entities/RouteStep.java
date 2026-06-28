package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RouteStep(
        Double distance,
        Double duration,
        String name,
        String mode,
        String driving_side,
        Double weight,
        Geometry geometry,
        StepManeuver maneuver,
        List<Intersection> intersections
) {
}