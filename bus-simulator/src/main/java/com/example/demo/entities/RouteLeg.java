package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RouteLeg(
        Double distance,
        Double duration,
        String summary,
        List<RouteStep> steps,
        Annotation annotation
) {
}