package ru.yandex.practicum.telemetry.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.service.handlers.hub.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.service.handlers.sensors.SensorEventHandler;

import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler<?>> sensorEventHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler<?>> hubEventHandlers;

    public EventController(List<SensorEventHandler<?>> sensorEventHandlerList,
                           List<HubEventHandler<?>> hubEventHandlerList) {
        this.sensorEventHandlers = sensorEventHandlerList.stream()
                .collect(Collectors.toMap(SensorEventHandler::getMessageType, Function.identity()));
        this.hubEventHandlers = hubEventHandlerList.stream()
                .collect(Collectors.toMap(HubEventHandler::getMessageType, Function.identity()));
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            if (sensorEventHandlers.containsKey(request.getPayloadCase())) {
                sensorEventHandlers.get(request.getPayloadCase()).handle(request);
                log.info("Данные сенсора: {} получены и отправлены в обработчик", request);
            } else {
                log.info("Данные сенсора: {} не обработаны", request);
                throw new ValidationException("Не возмозможно найти обработчик для события " +
                        request.getPayloadCase());
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            if (hubEventHandlers.containsKey(request.getPayloadCase())) {
                hubEventHandlers.get(request.getPayloadCase()).handle(request);
                log.info("Данные датчика: {} полученые и отправлены в обработчик", request);
            } else {
                log.info("Данные датчика: {} не полученые", request);
                throw new ValidationException("Не возмозможно найти обработчик для события " +
                        request.getPayloadCase());
            }
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
