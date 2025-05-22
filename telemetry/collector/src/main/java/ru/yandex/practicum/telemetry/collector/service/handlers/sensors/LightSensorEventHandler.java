package ru.yandex.practicum.telemetry.collector.service.handlers.sensors;

import ru.yandex.practicum.telemetry.collector.model.sensors.BaseSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensors.LightSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensors.SensorEventType;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

@Component
public class LightSensorEventHandler extends BaseSensorEventHandler<LightSensorAvro> {

    protected LightSensorEventHandler(KafkaProducerService kafkaProducerService, String sensorTopic) {
        super(kafkaProducerService, sensorTopic);
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }

    @Override
    protected LightSensorAvro convertToAvro(BaseSensorEvent sensorEvent) {
        LightSensorEvent _event = (LightSensorEvent) sensorEvent;

        return LightSensorAvro.newBuilder()
                .setLinkQuality(_event.getLinkQuality())
                .setLuminosity(_event.getLuminosity())
                .build();
    }
}
