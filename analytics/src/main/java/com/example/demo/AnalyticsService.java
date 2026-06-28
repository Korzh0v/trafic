package com.example.demo;

import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    public void process(String key, String value) {
        System.out.println("Key: " + key);
        System.out.println("Value: " + value);
    }
}
