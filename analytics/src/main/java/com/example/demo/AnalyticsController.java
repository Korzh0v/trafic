package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public Collection<BusAnalyticsSnapshot> all() {
        return analyticsService.snapshotAll();
    }

    @GetMapping("/{busNumber}")
    public BusAnalyticsSnapshot byBus(@PathVariable int busNumber) {
        return analyticsService.snapshot(busNumber);
    }
}