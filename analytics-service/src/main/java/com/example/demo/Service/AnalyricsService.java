package com.example.demo.Service;

import com.example.demo.DTO.BusDTO;
import com.example.demo.DTO.BusEventDTO;
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
public class AnalyticsService {}
