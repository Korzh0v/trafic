package com.example.demo.Service;

import com.example.demo.entities.Bus;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BusService {

    private Bus bus = new Bus();

    @Autowired
    private KafkaTemplate<String, Bus> template;

    @KafkaListener(topics = { "bus-telemetry" })
    @Scheduled(fixedRate = 500)
    public void sendMessage() {
        bus.moveVehicle();
        ProducerRecord<String, Bus> record = new ProducerRecord<String, Bus>(
            "bus-telemetry",
            bus
        );
        template.send(record);
    }
}
