package ru.yandex.practicum.telemetry.collector.service.handlers.sensors;

import ru.yandex.practicum.telemetry.collector.model.sensors.BaseSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensors.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensors.TemperatureSensorEvent;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.TemperatureSensorAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

@Component
public class TemperatureSensorEventHandler extends BaseSensorEventHandler<TemperatureSensorAvro> {

    protected TemperatureSensorEventHandler(KafkaProducerService kafkaProducerService, String sensorTopic) {
        super(kafkaProducerService, sensorTopic);
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.TEMPERATURE_SENSOR_EVENT;
    }

    @Override
    protected TemperatureSensorAvro convertToAvro(BaseSensorEvent sensorEvent) {
        TemperatureSensorEvent _event = (TemperatureSensorEvent) sensorEvent;

        return TemperatureSensorAvro.newBuilder()
                .setId(_event.getId())
                .setHubId(_event.getHubId())
                .setTimestamp(_event.getTimestamp())
                .setTemperatureC(_event.getTemperatureC())
                .setTemperatureF(_event.getTemperatureF())
                .build();

    }
}
