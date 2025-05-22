package ru.yandex.practicum.telemetry.collector.service.handlers.sensors;

import ru.yandex.practicum.telemetry.collector.model.sensors.BaseSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensors.MotionSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensors.SensorEventType;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

@Component
public class MotionSensorEventHandler extends BaseSensorEventHandler<MotionSensorAvro>{

    protected MotionSensorEventHandler(KafkaProducerService kafkaProducerService) {
        super(kafkaProducerService);
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }

    @Override
    protected MotionSensorAvro convertToAvro(BaseSensorEvent sensorEvent) {
        MotionSensorEvent _event = (MotionSensorEvent) sensorEvent;

        return MotionSensorAvro.newBuilder()
                .setLinkQuality(_event.getLinkQuality())
                .setMotion(_event.getMotion())
                .setVoltage(_event.getVoltage())
                .build();
    }
}
