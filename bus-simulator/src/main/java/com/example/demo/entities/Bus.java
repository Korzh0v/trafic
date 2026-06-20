package com.example.demo.entities;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bus {

    private int number;
    private final List<Coordinate> historyRoute = new ArrayList<>();

    private double lat;
    private double lng;

    private BusRoute route;
    private int currentPointIndex = 0;

    public Bus(int number, BusRoute route) {
        this.number = number;
        this.route = route;

        if (route != null && route.getRouteSize() > 0) {
            Coordinate start = route.getWaypoints().get(0);
            this.lat = start.getLat();
            this.lng = start.getLng();
            this.historyRoute.add(start);
        }
    }

    public void moveVehicle() {
        if (route == null || route.getRouteSize() == 0) return;

        currentPointIndex++;

        if (currentPointIndex >= route.getRouteSize()) {
            currentPointIndex = 0;
        }

        Coordinate nextPoint = route.getWaypoints().get(currentPointIndex);
        this.lat = nextPoint.getLat();
        this.lng = nextPoint.getLng();

        if (this.historyRoute.size() >= 10) {
            this.historyRoute.remove(0);
        }
        this.historyRoute.add(nextPoint);
    }
}