package ru.yandex.practicum.telemetry.collector.service.handlers.hub;

import ru.yandex.practicum.telemetry.collector.model.hub.BaseDeviceEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceEventType;
import ru.yandex.practicum.telemetry.collector.model.hub.ScenarioAddedEvent;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

import java.util.List;

@Component
public class ScenarioAddedEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {

    protected ScenarioAddedEventHandler(KafkaProducerService kafkaProducerService) {
        super(kafkaProducerService);
    }

    @Override
    public DeviceEventType getMessageType() {
        return DeviceEventType.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro convertToAvro(BaseDeviceEvent deviceEvent) {
        ScenarioAddedEvent _event = (ScenarioAddedEvent) deviceEvent;

        List<ScenarioConditionAvro> scenarioConditionAvros = _event.getConditions()
                .stream()
                .map( s -> ScenarioConditionAvro.newBuilder()
                        .setSensorId(s.getSensorId())
                        .setType(ConditionTypeAvro.valueOf(s.getType().name()))
                        .setOperation(ConditionOperationAvro.valueOf(s.getOperation().name()))
                        .setValue(s.getValue())
                        .build())
                .toList();

        List<DeviceActionAvro> deviceActionAvros = _event.getActions()
                .stream()
                .map( d -> DeviceActionAvro.newBuilder()
                        .setSensorId(d.getSensorId())
                        .setType(ActionTypeAvro.valueOf(d.getType().name()))
                        .setValue(d.getValue())
                        .build())
                .toList();

        return ScenarioAddedEventAvro.newBuilder()
                .setName(_event.getName())
                .setConditions(scenarioConditionAvros)
                .setActions(deviceActionAvros)
                .build();
    }
}
