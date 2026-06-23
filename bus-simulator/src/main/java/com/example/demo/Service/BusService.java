package com.example.demo.Service;

import com.example.demo.entities.Bus;
import com.example.demo.entities.RouteResponse;
import com.example.demo.entities.Waypoint;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BusService {

    private static final double EARTH_RADIUS = 6371000.0;

    @Autowired
    private KafkaTemplate<String, Bus> template;

    @Autowired
    private RestTemplate restTemplate;

    private Bus bus;

    @PostConstruct
    public void init () {
        bus = new Bus(1, getRoute());
    }

    @Scheduled(fixedRate = 500)
    public void sendMessage() {

        RouteResponse route = bus.getRoute();

        bus.moveVehicle();

        int currentIndex = 0;

        Waypoint from = route.waypoints().get(currentIndex);
        Waypoint to = route.waypoints().get(currentIndex + 1);

        double progress = progress(
                from,
                to,
                bus.getLat(),
                bus.getLng()
        );

        double distanceToNextStop = distanceToNextWaypoint(
                to,
                bus.getLat(),
                bus.getLng()
        );



        System.out.println("Progress = " + progress);
        System.out.println("Distance to next stop = " + distanceToNextStop + " m");

        ProducerRecord<String, Bus> record = new ProducerRecord<>(
                "bus-telemetry",
                String.valueOf(bus.getNumber()),
                bus
        );

        template.send(record);
    }

    public RouteResponse getRoute() {

        ResponseEntity<RouteResponse> response =
                restTemplate.exchange(
                        "https://router.project-osrm.org/route/v1/driving/" +
                                "30.467663,50.450837;30.524998,50.450181" +
                                "?steps=true&geometries=geojson",
                        HttpMethod.GET,
                        null,
                        RouteResponse.class
                );

        return response.getBody();
    }

    /**
     * Відстань між двома координатами.
     */
    private double haversine(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        double c =
                2 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * Довжина відрізка між waypoint.
     */
    private double segmentLength(
            Waypoint from,
            Waypoint to) {

        return haversine(
                from.location().get(1),
                from.location().get(0),
                to.location().get(1),
                to.location().get(0)
        );
    }

    /**
     * Скільки вже проїхав автобус на поточному відрізку.
     */
    private double travelledDistance(
            Waypoint from,
            double busLat,
            double busLng) {

        return haversine(
                from.location().get(1),
                from.location().get(0),
                busLat,
                busLng
        );
    }

    /**
     * Прогрес від 0 до 1.
     */
    private double progress(
            Waypoint from,
            Waypoint to,
            double busLat,
            double busLng) {

        double segmentLength = segmentLength(from, to);

        if (segmentLength == 0) {
            return 1.0;
        }

        double travelled = travelledDistance(
                from,
                busLat,
                busLng
        );

        double progress = travelled / segmentLength;

        return Math.max(
                0.0,
                Math.min(progress, 1.0)
        );
    }

    /**
     * Відстань до наступного waypoint.
     */
    private double distanceToNextWaypoint(
            Waypoint nextWaypoint,
            double busLat,
            double busLng) {

        return haversine(
                busLat,
                busLng,
                nextWaypoint.location().get(1),
                nextWaypoint.location().get(0)
        );
    }

    /**
     * Відстань до кінця поточного сегмента.
     */
    private double distanceToSegmentEnd(
            Waypoint from,
            Waypoint to,
            double busLat,
            double busLng) {

        double segmentLength = segmentLength(from, to);

        double progress = progress(
                from,
                to,
                busLat,
                busLng
        );

        return segmentLength * (1.0 - progress);
    }

    /**
     * Відстань до будь-якого waypoint попереду.
     */
    private double distanceToWaypoint(
            RouteResponse route,
            int currentIndex,
            int targetIndex,
            double busLat,
            double busLng) {

        if (targetIndex <= currentIndex) {
            return 0;
        }

        Waypoint nextWaypoint =
                route.waypoints().get(currentIndex + 1);

        double total = distanceToNextWaypoint(
                nextWaypoint,
                busLat,
                busLng
        );

        for (int i = currentIndex + 1; i < targetIndex; i++) {

            Waypoint from = route.waypoints().get(i);
            Waypoint to = route.waypoints().get(i + 1);

            total += segmentLength(from, to);
        }

        return total;
    }
}