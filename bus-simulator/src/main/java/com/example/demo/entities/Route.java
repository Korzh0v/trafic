package com.example.demo.entities;

import com.example.demo.entities.Geometry;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Route(
        Double distance,
        Double duration,
        Geometry geometry,
        List<RouteLeg> legs
) {
}