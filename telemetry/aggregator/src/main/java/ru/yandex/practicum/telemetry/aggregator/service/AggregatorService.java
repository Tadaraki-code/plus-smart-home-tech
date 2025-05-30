package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class AggregatorService {
    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        log.info("Получили событие на обработку: {}", event);
        SensorsSnapshotAvro snapshotAvro = snapshots.getOrDefault(event.getHubId(), new SensorsSnapshotAvro());

        if (snapshotAvro.getSensorsState() == null) {
            snapshotAvro.setHubId(event.getHubId());
            snapshotAvro.setSensorsState(new HashMap<>());
        }
        SensorStateAvro oldState = snapshotAvro.getSensorsState().get(event.getId());

        if (oldState != null) {
            if (oldState.getTimestamp().isAfter(event.getTimestamp()) ||
                    oldState.getData().equals(event.getPayload())) {
                log.info("Событие обработано, обновление не требуется: {}", event);
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        snapshotAvro.setTimestamp(event.getTimestamp());
        snapshotAvro.getSensorsState().put(event.getId(), newState);
        snapshots.put(event.getHubId(), snapshotAvro);

        log.info("Событие обработано, снепшёт с hubId, обновлён: {}", event.getHubId());
        return Optional.of(snapshotAvro);
    }
}
