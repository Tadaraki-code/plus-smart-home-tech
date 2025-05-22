package ru.yandex.practicum.telemetry.collector.service.handlers.sensors;

import ru.yandex.practicum.telemetry.collector.model.sensors.BaseSensorEvent;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

public abstract class BaseSensorEventHandler<T extends SpecificRecordBase> implements SensorEventHandler<T> {

    private final KafkaProducerService kafkaProducerService;
    private final String topic = "telemetry.sensors.v1";

    protected BaseSensorEventHandler(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public void handle(BaseSensorEvent sensorEvent) {
        T payload = convertToAvro(sensorEvent);
        SensorEventAvro result = SensorEventAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setTimestamp(sensorEvent.getTimestamp())
                .setPayload(payload)
                .build();

        kafkaProducerService.sendToKafka(topic, sensorEvent.getId(), result);
    }

    protected abstract T convertToAvro(BaseSensorEvent sensorEvent);
}
