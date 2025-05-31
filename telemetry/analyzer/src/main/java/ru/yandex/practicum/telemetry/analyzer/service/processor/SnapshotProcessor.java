package ru.yandex.practicum.telemetry.analyzer.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.service.SnapshotService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<String, SensorsSnapshotAvro> kafkaSensorsSnapshotConsumer;
    private final String snapshotTopic;
    private final SnapshotService snapshotService;

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaSensorsSnapshotConsumer::wakeup));
        try {
            kafkaSensorsSnapshotConsumer.subscribe(List.of(snapshotTopic));
            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = kafkaSensorsSnapshotConsumer
                        .poll(Duration.ofSeconds(4));

                int count = 0;
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    handleRecord(record);
                    manageOffsets(record, count, kafkaSensorsSnapshotConsumer);
                    count++;
                }
                kafkaSensorsSnapshotConsumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.info("Споймано WakeupException в SnapshotProcessor {}", ignored.getMessage());
        } catch (Exception e) {
            log.error("Ошибка во время обработки снепшота", e);
        } finally {

            try {
                kafkaSensorsSnapshotConsumer.commitSync(currentOffsets);

            } finally {
                log.info("Закрываем  снэпшот консьюмер");
                kafkaSensorsSnapshotConsumer.close();
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<String, SensorsSnapshotAvro> record,
                                      int count, KafkaConsumer<String, SensorsSnapshotAvro> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

    private void handleRecord(ConsumerRecord<String, SensorsSnapshotAvro> record) {
        snapshotService.handleSnapshot(record.value());
    }

}
