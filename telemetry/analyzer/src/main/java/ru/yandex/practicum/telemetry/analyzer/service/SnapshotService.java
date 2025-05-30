package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.grpc.GrpcClient;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.time.Instant;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotService {
    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final GrpcClient grpcClient;

    @Transactional(readOnly = true)
    public void handleSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        sensorsState.keySet().forEach(sensorId -> validateSensor(hubId, sensorId));

        scenarioRepository.findByHubId(snapshot.getHubId()).
                forEach(scenario -> checkAndExecuteScenario(scenario, sensorsState, snapshot.getTimestamp()));

        log.info("Обработан снапшот для хаба {} с {} датчиками", hubId, sensorsState.size());
    }

    private void checkAndExecuteScenario(Scenario scenario, Map<String, SensorStateAvro> sensorsState,
                                         Instant timestamp) {
        try {
            boolean allConditionsMet = scenario.getConditions().entrySet().stream()
                    .allMatch(entry -> {
                        String sensorId = entry.getKey();
                        Condition condition = entry.getValue();
                        SensorStateAvro state = sensorsState.get(sensorId);
                        return state != null && checkCondition(condition, state.getData());
                    });

            if (allConditionsMet) {
                scenario.getActions().forEach((sensorId, action) -> executeAction(sensorId, action,
                        scenario.getHubId(), scenario.getName(), timestamp));
                log.info("Сценарий {} выполнен для хаба {}", scenario.getName(), scenario.getHubId());
            }
        } catch (Exception e) {
            log.error("Возникла ошибка при проверке совпадения условия с данными снепшота {}", e.getMessage());
        }
    }

    private boolean checkCondition(Condition condition, Object sensorData) {
        Integer sensorValue = extractSensorValue(condition.getType(), sensorData);
        if (sensorValue == null) {
            log.info("Не удалось извлечь значение для условия типа {}", condition.getType());
            return false;
        }

        return switch (condition.getOperation()) {
            case EQUALS -> sensorValue.equals(condition.getValue());
            case GREATER_THAN -> sensorValue > condition.getValue();
            case LOWER_THAN -> sensorValue < condition.getValue();
        };
    }

    private Integer extractSensorValue(ConditionType conditionType, Object sensorData) {
        switch (conditionType) {
            case TEMPERATURE:
                if (sensorData instanceof ClimateSensorAvro climate) {
                    return climate.getTemperatureC();
                } else if (sensorData instanceof TemperatureSensorAvro temp) {
                    return temp.getTemperatureC();
                }
                return null;
            case HUMIDITY:
                return sensorData instanceof ClimateSensorAvro climate ? climate.getHumidity() : null;
            case CO2LEVEL:
                return sensorData instanceof ClimateSensorAvro climate ? climate.getCo2Level() : null;
            case LUMINOSITY:
                return sensorData instanceof LightSensorAvro light ? light.getLuminosity() : null;
            case MOTION:
                return sensorData instanceof MotionSensorAvro motion ? (motion.getMotion() ? 1 : 0) : null;
            case SWITCH:
                return sensorData instanceof SwitchSensorAvro switchSensor ? (switchSensor.getState() ? 1 : 0) : null;
            default:
                return null;
        }
    }

    private void executeAction(String sensorId, Action action, String hubId,
                               String scenarioName, Instant snapshotTimestamp) {
        log.info("Отправляем действия сценария в hubRouter: тип={}, значение={}, для датчика={}",
                action.getType(), action.getValue(), sensorId);
        grpcClient.sendToHubRouter(sensorId, action, hubId, scenarioName, snapshotTimestamp);
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
        } catch (Exception e) {
            log.error("Возникла ошибка при проверке существования датчика в базе данных {}", e.getMessage());
        }
    }
}