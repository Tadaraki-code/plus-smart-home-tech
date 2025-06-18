package ru.yandex.practicum.telemetry.aggregator.configuration;

import kafka.deserializer.SensorEventDeserializer;
import kafka.serializer.GeneralAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.util.Properties;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    public KafkaProducer<String, SpecificRecordBase> kafkaProducer(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        KafkaProducer<String, SpecificRecordBase> producer = new KafkaProducer<>(props);

        log.info("Создан KafkaProducer: {}", bootstrapServers);

        return producer;
    }

    @Bean
    public KafkaConsumer<String, SensorEventAvro> kafkaConsumer(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers) {
        Properties props = new Properties();
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "AggregatorConsumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "aggregator-group");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class.getName());

        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 3072000);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 307200);

        KafkaConsumer<String, SensorEventAvro> consumer = new KafkaConsumer<>(props);

        log.info("Создан KafkaConsumer: {}", bootstrapServers);

        return consumer;
    }

    @Bean
    public String snapshotTopic(@Value("${kafka.topic.snapshot}") String hubTopic) {
        log.info("Настроен топик для хабов: {}", hubTopic);
        return hubTopic;
    }

    @Bean
    public String sensorTopic(@Value("${kafka.topic.sensors}") String sensorTopic) {
        log.info("Настроен топик для сенсоров: {}", sensorTopic);
        return sensorTopic;
    }
}

