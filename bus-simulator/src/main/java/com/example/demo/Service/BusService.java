package com.example.demo.Service;

import com.example.demo.entities.Bus;
import com.example.demo.entities.BusRoute;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class BusService {

    private Bus bus = new Bus(1, new BusRoute());

    @Autowired
    private KafkaTemplate<String, Bus> template;

    @Scheduled(fixedRate = 500)
    public void sendMessage() {
        bus.moveVehicle();
        ProducerRecord<String, Bus> record = new ProducerRecord<>(
                "bus-telemetry",
                String.valueOf(bus.getNumber()), // ключ = номер автобуса
                bus
        );
        template.send(record);
    }
}
