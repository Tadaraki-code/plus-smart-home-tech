package ru.yandex.practicum.telemetry.analyzer.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repository.ActionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.HashMap;


@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventService {
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;

    @Transactional
    public void handle(HubEventAvro hubEvent) {
        try {
            Object payload = hubEvent.getPayload();
            switch (payload) {
                case DeviceAddedEventAvro event -> handleDeviceAdded(hubEvent.getHubId(), event);
                case DeviceRemovedEventAvro event -> handleDeviceRemoved(event);
                case ScenarioAddedEventAvro event -> handleScenarioAdded(hubEvent.getHubId(), event);
                case ScenarioRemovedEventAvro event -> handleScenarioRemoved(event);
                default -> log.info("Неизвестный тип события: {}", payload.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при обработке события {}: {}", hubEvent, e.getMessage(), e);
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro event) {
        try {
            if (sensorRepository.existsById(event.getId())) {
                log.info("Датчик с ID {} уже существует", event.getId());
                return;
            }
            Sensor sensor = Sensor.builder()
                    .id(event.getId())
                    .hubId(hubId)
                    .build();
            sensorRepository.save(sensor);
            log.info("Добавлен датчик: {}", sensor);
        } catch (Exception e) {
            log.error("Ошибка при добавлении данных о сенсоре {}", e.getMessage());
        }
    }

    private void handleDeviceRemoved(DeviceRemovedEventAvro event) {
        try {
            String sensorId = event.getId();
            if (scenarioRepository.countConditionsBySensorId(sensorId) > 0 ||
                    scenarioRepository.countActionsBySensorId(sensorId) > 0) {
                log.warn("Нельзя удалить датчик {}, он используется в сценариях", sensorId);
                throw new IllegalStateException("Датчик используется в сценариях: " + sensorId);
            }
            sensorRepository.deleteById(sensorId);
            log.info("Удалён датчик: {}", sensorId);
        } catch (Exception e) {
            log.error("Ошибка при удалении датчика {}: {}", event.getId(), e.getMessage());
        }
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro event) {
        try {
            for (ScenarioConditionAvro condition : event.getConditions()) {
                validateSensor(hubId, condition.getSensorId());
            }
            for (DeviceActionAvro action : event.getActions()) {
                validateSensor(hubId, action.getSensorId());
            }

            Scenario scenario = Scenario.builder()
                    .name(event.getName())
                    .hubId(hubId)
                    .conditions(new HashMap<>())
                    .actions(new HashMap<>())
                    .build();

            for (ScenarioConditionAvro conditionAvro : event.getConditions()) {
                Integer value = getConditionValue(conditionAvro);

                Condition condition = Condition.builder()
                        .type(mapConditionType(conditionAvro.getType()))
                        .operation(mapConditionOperation(conditionAvro.getOperation()))
                        .value(value)
                        .build();
                conditionRepository.save(condition);

                scenario.getConditions().put(conditionAvro.getSensorId(), condition);
            }

            for (DeviceActionAvro actionAvro : event.getActions()) {
                Action action = Action.builder()
                        .type(mapActionType(actionAvro.getType()))
                        .value(actionAvro.getValue())
                        .build();
                actionRepository.save(action);

                scenario.getActions().put(actionAvro.getSensorId(), action);
            }

            scenarioRepository.save(scenario);
            log.info("Добавлен сценарий: {}", scenario);
        } catch (Exception e) {
            log.error("Ошибка сохранения сценария {} для hub_id {}: {}", event.getName(), hubId, e.getMessage());
        }
    }

    private void handleScenarioRemoved(ScenarioRemovedEventAvro event) {
        String name = event.getName();
        if (!scenarioRepository.existsByName(name)) {
            log.info("Сценарий с именем {} не найден", name);
            return;
        }
        scenarioRepository.deleteByName(name);
        log.info("Удалён сценарий: {}", name);
    }

    private void validateSensor(String hubId, String sensorId) {
        try {
            Sensor sensor = sensorRepository.findById(sensorId)
                    .orElseThrow(() -> new IllegalArgumentException("Датчик не найден: " + sensorId));
            if (!hubId.equals(sensor.getHubId())) {
                throw new IllegalArgumentException(
                        String.format("Несоответствие hub_id для датчика %s: ожидалось %s, найдено %s",
                                sensorId, hubId, sensor.getHubId()));
            }
        } catch (IllegalArgumentException e) {
            log.error("Ошибка валидации датчика {}: {}", sensorId, e.getMessage());
            throw e;
        }
    }

    private Integer getConditionValue(ScenarioConditionAvro conditionAvro) {
        Object value = conditionAvro.getValue();
        return switch (value) {
            case null -> null;
            case Integer intValue -> intValue;
            case Boolean boolValue -> boolValue ? 1 : 0;
            default -> throw new IllegalArgumentException(
                    String.format("Неподдерживаемый тип значения " +
                            "условия: %s, ожидается Integer или Boolean", value.getClass().getName())
            );
        };
    }

    private ConditionType mapConditionType(ConditionTypeAvro avroType) {
        return switch (avroType) {
            case MOTION -> ConditionType.MOTION;
            case LUMINOSITY -> ConditionType.LUMINOSITY;
            case SWITCH -> ConditionType.SWITCH;
            case TEMPERATURE -> ConditionType.TEMPERATURE;
            case CO2LEVEL -> ConditionType.CO2LEVEL;
            case HUMIDITY -> ConditionType.HUMIDITY;
        };
    }

    private ConditionOperation mapConditionOperation(ConditionOperationAvro avroOperation) {
        return switch (avroOperation) {
            case EQUALS -> ConditionOperation.EQUALS;
            case GREATER_THAN -> ConditionOperation.GREATER_THAN;
            case LOWER_THAN -> ConditionOperation.LOWER_THAN;
        };
    }

    private ActionType mapActionType(ActionTypeAvro avroType) {
        return switch (avroType) {
            case ACTIVATE -> ActionType.ACTIVATE;
            case DEACTIVATE -> ActionType.DEACTIVATE;
            case INVERSE -> ActionType.INVERSE;
            case SET_VALUE -> ActionType.SET_VALUE;
        };
    }
}
