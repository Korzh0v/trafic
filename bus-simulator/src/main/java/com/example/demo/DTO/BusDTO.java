package com.example.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@NoArgsConstructor
@AllArgsConstructor
@Setter
public class BusDTO {
    private int busNumber;
    private double lat;
    private double lng;
    private double progress;
    private double distanceToNextStop;


}
