package com.example.demo;

import org.apache.kafka.streams.KafkaStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    @Autowired
    public KafkaStreams streams;

    public void makeAnalytics() {}
}
