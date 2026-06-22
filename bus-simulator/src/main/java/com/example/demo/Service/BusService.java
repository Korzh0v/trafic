package com.example.demo.Service;

import com.example.demo.entities.Bus;
import com.example.demo.entities.RouteResponse;
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

    @Autowired
    private KafkaTemplate<String, Bus> template;

    Bus bus = new Bus(1, getRoute());

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedRate = 500)
    public void sendMessage() {
        RouteResponse route = bus.getRoute();
        double currentPosition = bus.getLat() / bus.getLng();
        route.waypoints().removeFirst();

        ProducerRecord<String, Bus> record = new ProducerRecord<>(
            "bus-telemetry",
            String.valueOf(bus.getNumber()), // ключ = номер автобуса
            bus
        );
        template.send(record);
    }

    public RouteResponse getRoute() {
        ResponseEntity<RouteResponse> response = restTemplate.exchange(
            "https://router.project-osrm.org/route/v1/driving/" +
                "30.467663,50.450837;30.524998,50.450181" +
                "?steps=true&geometries=geojson",
            HttpMethod.GET,
            null,
            RouteResponse.class
        );

        RouteResponse body = response.getBody();
        return body;
    }
}
