package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RouteResponse(
        String code,
        List<Waypoint> waypoints,
        List<Route> routes
) {
}