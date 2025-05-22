package ru.yandex.practicum.telemetry.collector.service.handlers.hub;

import ru.yandex.practicum.telemetry.collector.model.hub.BaseDeviceEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceAddedEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceEventType;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

@Component
public class DeviceAddedEventHandler extends BaseHubEventHandler<DeviceAddedEventAvro> {

    protected DeviceAddedEventHandler(KafkaProducerService kafkaProducerService, String hubTopic) {
        super(kafkaProducerService, hubTopic);
    }

    @Override
    public DeviceEventType getMessageType() {
        return DeviceEventType.DEVICE_ADDED;
    }

    @Override
    protected DeviceAddedEventAvro convertToAvro(BaseDeviceEvent deviceEvent) {
        DeviceAddedEvent _event = (DeviceAddedEvent) deviceEvent;

        return DeviceAddedEventAvro.newBuilder()
                .setId(_event.getId())
                .setType(DeviceTypeAvro.valueOf(_event.getDeviceType().name()))
                .build();
    }
}
