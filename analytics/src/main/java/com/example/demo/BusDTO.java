package com.example.demo;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.*;
import org.springframework.stereotype.Service;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class BusDTO {
    private Integer busNumber;
    private Double lat;
    private Double lng;
    private Double progress;
    private Double distanceToNextStop;
    private Integer passengers;

}
