package ru.yandex.practicum.telemetry.collector.service.handlers.sensors;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.TemperatureSensorProto;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

import java.time.Instant;

@Component
public class TemperatureSensorEventHandler extends BaseSensorEventHandler<TemperatureSensorAvro> {

    protected TemperatureSensorEventHandler(KafkaProducerService kafkaProducerService, String sensorTopic) {
        super(kafkaProducerService, sensorTopic);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR_EVENT;
    }

    @Override
    protected TemperatureSensorAvro convertToAvro(SensorEventProto sensorEvent) {
        TemperatureSensorProto _event = sensorEvent.getTemperatureSensorEvent();

        Instant eventTimestamp = Instant.ofEpochSecond(
                sensorEvent.getTimestamp().getSeconds(),
                sensorEvent.getTimestamp().getNanos()
        );

        return TemperatureSensorAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setTimestamp(eventTimestamp)
                .setTemperatureC(_event.getTemperatureC())
                .setTemperatureF(_event.getTemperatureF())
                .build();

    }
}
