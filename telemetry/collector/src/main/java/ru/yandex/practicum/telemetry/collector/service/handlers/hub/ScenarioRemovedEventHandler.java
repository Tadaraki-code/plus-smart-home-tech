package ru.yandex.practicum.telemetry.collector.service.handlers.hub;

import ru.yandex.practicum.telemetry.collector.model.hub.BaseDeviceEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioRemovedEvent;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

@Component
public class ScenarioRemovedEventHandler extends BaseHubEventHandler<ScenarioRemovedEventAvro> {

    protected ScenarioRemovedEventHandler(KafkaProducerService kafkaProducerService) {
        super(kafkaProducerService);
    }

    @Override
    public DeviceEventType getMessageType() {
        return DeviceEventType.SCENARIO_REMOVED;
    }

    @Override
    protected ScenarioRemovedEventAvro convertToAvro(BaseDeviceEvent deviceEvent) {
        ScenarioRemovedEvent _event = (ScenarioRemovedEvent) deviceEvent;

        return ScenarioRemovedEventAvro.newBuilder()
                .setName(_event.getName())
                .build();
    }
}
