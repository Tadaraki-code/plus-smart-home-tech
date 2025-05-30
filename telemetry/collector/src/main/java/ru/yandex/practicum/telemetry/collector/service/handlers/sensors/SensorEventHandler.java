package ru.yandex.practicum.telemetry.collector.service.handlers.sensors;

import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

public interface SensorEventHandler<T> {
    SensorEventProto.PayloadCase getMessageType();

    void handle(SensorEventProto sensorEvent);
}
