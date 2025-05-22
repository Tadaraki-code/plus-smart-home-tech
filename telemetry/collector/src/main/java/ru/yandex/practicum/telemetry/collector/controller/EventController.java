package ru.yandex.practicum.telemetry.collector.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.telemetry.collector.model.hub.BaseDeviceEvent;
import ru.yandex.practicum.telemetry.collector.model.hub.DeviceEventType;
import ru.yandex.practicum.telemetry.collector.model.sensors.BaseSensorEvent;
import ru.yandex.practicum.telemetry.collector.model.sensors.SensorEventType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.telemetry.collector.service.handlers.hub.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.service.handlers.sensors.SensorEventHandler;

import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/events")
public class EventController {
    private final Map<SensorEventType, SensorEventHandler<?>> sensorEventHandlers;
    private final Map<DeviceEventType, HubEventHandler<?>> hubEventHandlers;

    public EventController(List<SensorEventHandler<?>> sensorEventHandlerList,
                           List<HubEventHandler<?>> hubEventHandlerList) {
        this.sensorEventHandlers = sensorEventHandlerList.stream()
                .collect(Collectors.toMap(SensorEventHandler::getMessageType, Function.identity()));
        this.hubEventHandlers = hubEventHandlerList.stream()
                .collect(Collectors.toMap(HubEventHandler::getMessageType, Function.identity()));
    }

    @PostMapping("/sensors")
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody BaseSensorEvent sensorEvent) {
        if(sensorEventHandlers.containsKey(sensorEvent.getType())) {
            sensorEventHandlers.get(sensorEvent.getType()).handle(sensorEvent);
            log.info("Данные сенсора: {} полученые и отправлены в обработчик", sensorEvent);
            return ResponseEntity.ok().build();
        } else {
            log.info("Данные сенсора : {} не полученые", sensorEvent);
            throw new ValidationException("Не возмозможно найти обработчик для события " + sensorEvent.getType());
        }
    }

    @PostMapping("/hubs")
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody BaseDeviceEvent deviceEvent) {
        if(hubEventHandlers.containsKey(deviceEvent.getType())) {
            hubEventHandlers.get(deviceEvent.getType()).handle(deviceEvent);
            log.info("Данные датчика: {} полученые и отправлены в обработчик", deviceEvent);
            return ResponseEntity.ok().build();
        } else {
            log.info("Данные датчика: {} не полученые", deviceEvent);
            throw new ValidationException("Не возмозможно найти обработчик для события " + deviceEvent.getType());
        }
    }
}
