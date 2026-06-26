package com.example.demo.entities;

import com.example.demo.DTO.BusEventDTO;
import java.util.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Bus {

    private int number;

    private final List<double[]> historyRoute = new ArrayList<>();
    private Set<Integer> stopIndices = new HashSet<>();

    private int accidentTicks = 0;
    private int trafficTicks = 0;
    private int waitTicks = 0;

    private double lat;
    private double lng;

    private int passengers;

    private RouteResponse route;

    private List<double[]> path = new ArrayList<>();

    private int currentIndex = 0;
    private double progress;
    private double distanceToNextStop;
    private Random rand = new Random();

    private static final double ACCIDENT_CHANCE = 0.002;
    private static final double TRAFFIC_CHANCE = 0.02;
    private static final int STOP_DURATION_TICKS = 3;

    private int totalStops = 0;
    private int totalTrafficEvents = 0;
    private int totalAccidents = 0;
    private long totalDelayTicks = 0;
    private int maxPassengers = 0;

    private boolean wasAtStop = false;

    public Bus(int number, RouteResponse route) {
        this.number = number;
        this.route = route;
        this.passengers = 0;
        if (
            route != null && route.routes() != null && !route.routes().isEmpty()
        ) {
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
        stopIndices.clear();

        for (Route r : response.routes()) {
            if (r.legs() == null) continue;

            for (RouteLeg leg : r.legs()) {
                if (leg.steps() == null) continue;

                for (RouteStep step : leg.steps()) {
                    if (
                        step.geometry() == null ||
                        step.geometry().coordinates() == null
                    ) continue;

                    for (List<Double> c : step.geometry().coordinates()) {
                        if (c.size() >= 2) {
                            result.add(new double[] { c.get(1), c.get(0) }); // [lat, lng]
                        }
                    }

                    if (!result.isEmpty()) {
                        stopIndices.add(result.size() - 1);
                    }
                }
            }
        }

        // fallback: route.geometry якщо steps порожні
        if (result.isEmpty()) {
            for (Route r : response.routes()) {
                if (
                    r.geometry() == null || r.geometry().coordinates() == null
                ) continue;

                for (List<Double> c : r.geometry().coordinates()) {
                    if (c.size() >= 2) {
                        result.add(new double[] { c.get(1), c.get(0) }); // [lat, lng]
                    }
                }
            }
        }

        return result;
    }

    public BusEventDTO moveVehicle() {
        if (path.isEmpty()) return null;

        if (!isAtStop()) {
            if (accidentTicks > 0) {
                accidentTicks--;
                return new BusEventDTO(
                    // ← було null
                    this.number,
                    "ACCIDENT",
                    this.lat,
                    this.lng,
                    accidentTicks,
                    this.passengers,
                    System.currentTimeMillis()
                );
            }
            if (trafficTicks > 0) {
                trafficTicks--;
                return new BusEventDTO(
                    // ← було null
                    this.number,
                    "TRAFFIC",
                    this.lat,
                    this.lng,
                    trafficTicks,
                    this.passengers,
                    System.currentTimeMillis()
                );
            }
            if (rand.nextDouble() < TRAFFIC_CHANCE) {
                trafficTicks = rand.nextInt(3, 10);
                totalTrafficEvents++;
                totalDelayTicks += trafficTicks;
                return new BusEventDTO(
                    this.number,
                    "TRAFFIC",
                    this.lat,
                    this.lng,
                    trafficTicks,
                    this.passengers,
                    System.currentTimeMillis()
                );
            }
            if (rand.nextDouble() < ACCIDENT_CHANCE) {
                accidentTicks = rand.nextInt(20, 50);
                totalAccidents++;
                totalDelayTicks += accidentTicks;
                return new BusEventDTO(
                    this.number,
                    "ACCIDENT",
                    this.lat,
                    this.lng,
                    accidentTicks,
                    this.passengers,
                    System.currentTimeMillis()
                );
            }
        }
        if (waitTicks > 0) {
            waitTicks--;
            return new BusEventDTO(
                this.number,
                "AT_STOP",
                this.lat,
                this.lng,
                waitTicks,
                this.passengers,
                System.currentTimeMillis()
            );
        }

        currentIndex++;

        if (currentIndex >= path.size()) {
            currentIndex = 0;
            passengers = 0;
        }

        double[] next = path.get(currentIndex);
        this.lat = next[0];
        this.lng = next[1];

        if (historyRoute.size() >= 10) {
            historyRoute.remove(0);
        }

        historyRoute.add(next);

        if (stopIndices.contains(currentIndex)) {
            waitTicks = STOP_DURATION_TICKS;
            totalStops++;
            if (passengers > maxPassengers) maxPassengers = passengers;
            if (passengers > 0) {
                passengers -= rand.nextInt(0, 3);
                if (passengers < 0) {
                    passengers = 0;
                }
            }
            passengers += rand.nextInt(0, 5);
        }

        boolean isAtStopNow = waitTicks > 0;

        if (!wasAtStop && isAtStopNow) {
            wasAtStop = true;
            return new BusEventDTO(
                this.number,
                "ARRIVED",
                this.lat,
                this.lng,
                this.passengers,
                waitTicks,
                System.currentTimeMillis()
            );
        }

        if (wasAtStop && !isAtStopNow) {
            wasAtStop = false;
            return new BusEventDTO(
                this.number,
                "DEPARTED",
                this.lat,
                this.lng,
                this.passengers,
                0,
                System.currentTimeMillis()
            );
        }
        return new BusEventDTO(
            this.number,
            "MOVING",
            this.lat,
            this.lng,
            0,
            this.passengers,
            System.currentTimeMillis()
        );
    }

    public boolean isAtStop() {
        return waitTicks > 0;
    }
}
