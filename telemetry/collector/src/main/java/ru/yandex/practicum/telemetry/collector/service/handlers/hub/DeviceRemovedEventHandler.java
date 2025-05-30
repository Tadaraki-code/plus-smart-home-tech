package ru.yandex.practicum.telemetry.collector.service.handlers.hub;

import ru.yandex.practicum.grpc.telemetry.event.DeviceRemovedEventProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

@Component
public class DeviceRemovedEventHandler extends BaseHubEventHandler<DeviceRemovedEventAvro> {

    protected DeviceRemovedEventHandler(KafkaProducerService kafkaProducerService, String hubTopic) {
        super(kafkaProducerService, hubTopic);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    protected DeviceRemovedEventAvro convertToAvro(HubEventProto deviceEvent) {
        DeviceRemovedEventProto _event = deviceEvent.getDeviceRemoved();

        return DeviceRemovedEventAvro.newBuilder()
                .setId(_event.getId())
                .build();
    }
}
