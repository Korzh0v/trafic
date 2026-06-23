package com.example.demo.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Bus {

    private int number;

    private final List<double[]> historyRoute = new ArrayList<>();

    private double lat;
    private double lng;

    private RouteResponse route;

    private List<double[]> path = new ArrayList<>();

    private int currentIndex = 0;
    private double progress;
    private double distanceToNextStop;

    public Bus(int number, RouteResponse route) {
        this.number = number;
        this.route = route;

        if (route != null && route.routes() != null && !route.routes().isEmpty()) {
            this.path = extractPath(route);

            if (!path.isEmpty()) {
                double[] start = path.get(0);
                this.lat = start[0];
                this.lng = start[1];
                this.historyRoute.add(start);
            }
        }
    }

    private List<double[]> extractPath(RouteResponse response) {
        List<double[]> result = new ArrayList<>();

        for (Route r : response.routes()) {
            if (r.legs() == null) continue;

            for (RouteLeg leg : r.legs()) {
                if (leg.steps() == null) continue;

                for (RouteStep step : leg.steps()) {
                    if (step.geometry() == null || step.geometry().coordinates() == null) continue;

                    for (List<Double> c : step.geometry().coordinates()) {
                        if (c.size() >= 2) {
                            result.add(new double[]{c.get(1), c.get(0)}); // [lat, lng]
                        }
                    }
                }
            }
        }

        // fallback: route.geometry якщо steps порожні
        if (result.isEmpty()) {
            for (Route r : response.routes()) {
                if (r.geometry() == null || r.geometry().coordinates() == null) continue;

                for (List<Double> c : r.geometry().coordinates()) {
                    if (c.size() >= 2) {
                        result.add(new double[]{c.get(1), c.get(0)}); // [lat, lng]
                    }
                }
            }
        }

        return result;
    }

    public void moveVehicle() {
        if (path.isEmpty()) return;

        currentIndex++;

        if (currentIndex >= path.size()) {
            currentIndex = 0;
        }

        double[] next = path.get(currentIndex);
        this.lat = next[0];
        this.lng = next[1];

        if (historyRoute.size() >= 10) {
            historyRoute.remove(0);
        }

        historyRoute.add(next);
    }
}