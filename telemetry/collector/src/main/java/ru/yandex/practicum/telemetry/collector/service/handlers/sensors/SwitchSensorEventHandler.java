package ru.yandex.practicum.telemetry.collector.service.handlers.sensors;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SwitchSensorProto;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

@Component
public class SwitchSensorEventHandler extends BaseSensorEventHandler<SwitchSensorAvro> {

    protected SwitchSensorEventHandler(KafkaProducerService kafkaProducerService, String sensorTopic) {
        super(kafkaProducerService, sensorTopic);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR_EVENT;
    }

    @Override
    protected SwitchSensorAvro convertToAvro(SensorEventProto sensorEvent) {
        SwitchSensorProto _event = sensorEvent.getSwitchSensorEvent();

        return SwitchSensorAvro.newBuilder()
                .setState(_event.getState())
                .build();
    }
}
