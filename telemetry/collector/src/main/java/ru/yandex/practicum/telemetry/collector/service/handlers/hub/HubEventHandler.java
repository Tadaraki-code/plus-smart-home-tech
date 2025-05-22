package ru.yandex.practicum.telemetry.collector.service.handlers.hub;

import ru.yandex.practicum.telemetry.collector.model.hub.BaseDeviceEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceEventType;

public interface HubEventHandler<T> {
    DeviceEventType getMessageType();

    void handle(BaseDeviceEvent event);
}
