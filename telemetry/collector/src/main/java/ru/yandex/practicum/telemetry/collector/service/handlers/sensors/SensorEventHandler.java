package ru.yandex.practicum.telemetry.collector.service.handlers.sensors;

import ru.yandex.practicum.telemetry.collector.model.sensors.BaseSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensors.SensorEventType;

public interface SensorEventHandler<T> {
    SensorEventType getMessageType();

    void handle(BaseSensorEvent sensorEvent);
}
