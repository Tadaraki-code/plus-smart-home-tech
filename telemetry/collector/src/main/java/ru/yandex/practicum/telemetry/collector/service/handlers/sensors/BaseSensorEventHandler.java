package ru.yandex.practicum.telemetry.collector.service.handlers.sensors;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

import java.time.Instant;

public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler<T> {

    private final KafkaProducerService kafkaProducerService;
    private final String topic;

    protected BaseSensorEventHandler(KafkaProducerService kafkaProducerService, String sensorTopic) {
        this.kafkaProducerService = kafkaProducerService;
        this.topic = sensorTopic;
    }

    @Override
    public void handle(SensorEventProto sensorEvent) {
        T payload = convertToAvro(sensorEvent);
        Instant eventTimestamp = Instant.ofEpochSecond(
                sensorEvent.getTimestamp().getSeconds(),
                sensorEvent.getTimestamp().getNanos()
        );
        SensorEventAvro result = SensorEventAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setTimestamp(eventTimestamp)
                .setPayload(payload)
                .build();

        kafkaProducerService.sendToKafka(topic, sensorEvent.getId(), result);
    }

    protected abstract T convertToAvro(SensorEventProto sensorEvent);
}
