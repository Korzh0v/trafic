package com.example.demo.entities;

import com.example.demo.entities.Coordinate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.scheduling.annotation.Scheduled;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bus {

    private int number = 1;
    private final List<Coordinate> route = new ArrayList<>();
    double lat = 50.4501;
    double lng = 30.5234;

    public void moveVehicle() {
        route.add(new Coordinate(lat, lng));
        lat += 0.0005;
        lng += 0.0012;
    }
}
