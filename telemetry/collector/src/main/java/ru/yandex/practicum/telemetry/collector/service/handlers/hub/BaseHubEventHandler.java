package ru.yandex.practicum.telemetry.collector.service.handlers.hub;

import ru.yandex.practicum.telemetry.collector.model.hub.BaseDeviceEvent;
import org.apache.avro.specific.SpecificRecordBase;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.collector.service.KafkaProducerService;

public abstract class BaseHubEventHandler <T extends SpecificRecordBase> implements HubEventHandler<T> {

    private final KafkaProducerService kafkaProducerService;
    private final String topic = "telemetry.hubs.v1";

    protected BaseHubEventHandler(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    public void handle(BaseDeviceEvent deviceEvent) {
        T payload = convertToAvro(deviceEvent);
        HubEventAvro result = HubEventAvro.newBuilder()
                .setHubId(deviceEvent.getHubId())
                .setTimestamp(deviceEvent.getTimestamp())
                .setPayload(payload)
                .build();

        kafkaProducerService.sendToKafka(topic, deviceEvent.getHubId(), result);
    }

    protected abstract T convertToAvro(BaseDeviceEvent deviceEvent);
}
