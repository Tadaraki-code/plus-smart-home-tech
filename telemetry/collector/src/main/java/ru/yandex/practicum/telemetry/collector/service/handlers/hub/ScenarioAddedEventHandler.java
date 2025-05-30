package ru.yandex.practicum.telemetry.collector.service.handlers.hub;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.ScenarioAddedEventProto;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

import java.util.List;

@Component
public class ScenarioAddedEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {

    protected ScenarioAddedEventHandler(KafkaProducerService kafkaProducerService, String hubTopic) {
        super(kafkaProducerService, hubTopic);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro convertToAvro(HubEventProto deviceEvent) {
        ScenarioAddedEventProto _event = deviceEvent.getScenarioAdded();

        List<ScenarioConditionAvro> scenarioConditionAvros = _event.getConditionList()
                .stream()
                .map(s -> {
                    ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                            .setSensorId(s.getSensorId())
                            .setType(ConditionTypeAvro.valueOf(s.getType().name()))
                            .setOperation(ConditionOperationAvro.valueOf(s.getOperation().name()));

                    switch (s.getValueCase()) {
                        case BOOL_VALUE -> builder.setValue(s.getBoolValue());
                        case INT_VALUE -> builder.setValue(s.getIntValue());
                        case VALUE_NOT_SET -> builder.setValue(null);
                    }

                    return builder.build();
                })
                .toList();

        List<DeviceActionAvro> deviceActionAvros = _event.getActionList()
                .stream()
                .map(d -> DeviceActionAvro.newBuilder()
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
