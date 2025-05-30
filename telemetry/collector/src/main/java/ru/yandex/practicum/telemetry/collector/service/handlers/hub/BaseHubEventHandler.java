package ru.yandex.practicum.telemetry.collector.service.handlers.hub;

import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

import java.time.Instant;

public abstract class BaseHubEventHandler <T extends SpecificRecordBase> implements HubEventHandler<T> {

    private final KafkaProducerService kafkaProducerService;
    private final String topic;

    protected BaseHubEventHandler(KafkaProducerService kafkaProducerService, String hubTopic) {
        this.kafkaProducerService = kafkaProducerService;
        this.topic = hubTopic;
    }

    @Override
    public void handle(HubEventProto deviceEvent) {
        T payload = convertToAvro(deviceEvent);

        Instant eventTimestamp = Instant.ofEpochSecond(
                deviceEvent.getTimestamp().getSeconds(),
                deviceEvent.getTimestamp().getNanos()
        );
        HubEventAvro result = HubEventAvro.newBuilder()
                .setHubId(deviceEvent.getHubId())
                .setTimestamp(eventTimestamp)
                .setPayload(payload)
                .build();

        kafkaProducerService.sendToKafka(topic, deviceEvent.getHubId(), result);
    }

    protected abstract T convertToAvro(HubEventProto deviceEvent);
}
