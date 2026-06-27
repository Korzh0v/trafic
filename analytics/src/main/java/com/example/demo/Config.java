package com.example.demo;

import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public KafkaStreams kStream() {
        return new KafkaStreams(topology(), streamConfig());
    }

    @Bean
    public Topology topology() {
        StreamsBuilder builder = new StreamsBuilder();
        builder.stream("transport.gps.raw");
        return builder.build();
    }

    @Bean
    public Properties streamConfig() {
        Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count-app");

        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        props.put(
            StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
            Serdes.String().getClass()
        );

        props.put(
            StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
            Serdes.String().getClass()
        );

        return props;
    }
}
