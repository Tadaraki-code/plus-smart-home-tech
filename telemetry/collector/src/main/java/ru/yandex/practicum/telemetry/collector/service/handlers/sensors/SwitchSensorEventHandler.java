package ru.yandex.practicum.telemetry.collector.service.handlers.sensors;

import ru.yandex.practicum.telemetry.collector.model.sensors.BaseSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensors.SensorEventType;
import ru.yandex.practicum.telemetry.collector.model.sensors.SwitchSensorEvent;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

@Component
public class SwitchSensorEventHandler extends BaseSensorEventHandler<SwitchSensorAvro> {

    protected SwitchSensorEventHandler(KafkaProducerService kafkaProducerService, String sensorTopic) {
        super(kafkaProducerService, sensorTopic);
    }

    @Override
    public SensorEventType getMessageType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }

    @Override
    protected SwitchSensorAvro convertToAvro(BaseSensorEvent sensorEvent) {
        SwitchSensorEvent _event = (SwitchSensorEvent) sensorEvent;

        return SwitchSensorAvro.newBuilder()
                .setState(_event.getState())
                .build();
    }
}
