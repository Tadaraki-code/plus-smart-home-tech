package ru.yandex.practicum.telemetry.collector.service.handlers.hub;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;

public interface HubEventHandler<T> {
    HubEventProto.PayloadCase getMessageType();

    void handle(HubEventProto event);
}
