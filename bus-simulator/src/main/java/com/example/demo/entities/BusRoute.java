package com.example.demo.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BusRoute {

    private List<Coordinate> waypoints = new ArrayList<>();

    public BusRoute() {
        waypoints.add(new Coordinate(50.4501, 30.5234));
        waypoints.add(new Coordinate(50.4522, 30.5280));
        waypoints.add(new Coordinate(50.4590, 30.5250));
        waypoints.add(new Coordinate(50.4650, 30.5100));
    }

    public int getRouteSize() {
        return waypoints.size();
    }
}