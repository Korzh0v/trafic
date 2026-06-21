package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Annotation(
        List<Double> distance,
        List<Double> duration,
        List<Integer> datasources,
        List<Long> nodes,
        List<Double> speed,
        List<Double> weight
) {
}