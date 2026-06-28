package com.example.demo.DTO;

import lombok.*;
import org.springframework.stereotype.Service;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BusDTO {
    private int busNumber;
    private double lat;
    private double lng;
    private double progress;
    private double distanceToNextStop;
    private int passengers;

}
