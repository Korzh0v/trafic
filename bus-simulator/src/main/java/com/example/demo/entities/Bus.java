package com.example.demo.entities;

import com.example.demo.entities.Coordinate;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;

public class Bus {
    private final List<Coordinate> route = new ArrayList<>();
    double lat = 50.4501;
    double lng = 30.5234;


    @Scheduled(fixedRate = 500)
    public void moveVehicle() {
        for (int i = 0; i < 100; i++) {
            route.add(new Coordinate(lat, lng));
            lat +=0.0005;
            lng += 0.0012;
        }
    }


}
