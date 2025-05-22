package ru.yandex.practicum.telemetry.collector.service.handlers.hub;

import ru.yandex.practicum.telemetry.collector.model.hub.BaseDeviceEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceRemovedEvent;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

@Component
public class DeviceRemovedEventHandler extends BaseHubEventHandler<DeviceRemovedEventAvro>{

    protected DeviceRemovedEventHandler(KafkaProducerService kafkaProducerService) {
        super(kafkaProducerService);
    }

    @Override
    public DeviceEventType getMessageType() {
        return DeviceEventType.DEVICE_REMOVED;
    }

    @Override
    protected DeviceRemovedEventAvro convertToAvro(BaseDeviceEvent deviceEvent) {
        DeviceRemovedEvent _event = (DeviceRemovedEvent) deviceEvent;

        return DeviceRemovedEventAvro.newBuilder()
                .setId(_event.getId())
                .build();
    }
}
