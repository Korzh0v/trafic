package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Intersection(
        List<Double> location,
        List<Integer> bearings,
        List<Boolean> entry,
        Integer in,
        Integer out
) {
}