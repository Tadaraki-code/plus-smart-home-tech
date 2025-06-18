package ru.yandex.practicum.telemetry.analyzer.configuration;

import kafka.deserializer.HubEventDeserializer;
import kafka.deserializer.SensorsSnapshotDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Properties;

@Slf4j
@Configuration
@ConfigurationProperties("analyzer.kafka")
public class KafkaConfig {

    @Bean
    public KafkaConsumer<String, HubEventAvro> kafkaHubEventConsumer(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        Properties props = new Properties();
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "AnalyzerHubEventConsumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "hub-analyzer-group");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, HubEventDeserializer.class.getName());

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 3072000);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 307200);

        KafkaConsumer<String, HubEventAvro> consumer = new KafkaConsumer<>(props);

        log.info("Создан kafkaHubEventConsumer: {}", bootstrapServers);

        return consumer;
    }

    @Bean
    public KafkaConsumer<String, SensorsSnapshotAvro> kafkaSensorsSnapshotConsumer(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        Properties props = new Properties();
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "AnalyzerSensorsSnapshotConsumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "snapshot-analyzer-group");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorsSnapshotDeserializer.class.getName());

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 3072000);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 307200);

        KafkaConsumer<String, SensorsSnapshotAvro> consumer = new KafkaConsumer<>(props);

        log.info("Создан kafkaSensorsSnapshotConsumer: {}", bootstrapServers);

        return consumer;
    }

    @Bean
    public String snapshotTopic(@Value("${kafka.topic.snapshot}") String snapshotTopic) {
        log.info("Настроен топик для хабов: {}", snapshotTopic);
        return snapshotTopic;
    }

    @Bean
    public String hubTopic(@Value("${kafka.topic.hub}") String hubTopic) {
        log.info("Настроен топик для сенсоров: {}", hubTopic);
        return hubTopic;
    }
}
