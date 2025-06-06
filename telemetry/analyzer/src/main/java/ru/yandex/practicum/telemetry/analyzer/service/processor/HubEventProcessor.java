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
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.service.HubEventService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<String, HubEventAvro> kafkaHubEventConsumer;
    private final String hubTopic;
    private final HubEventService hubEventService;

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaHubEventConsumer::wakeup));
        try {
            kafkaHubEventConsumer.subscribe(List.of(hubTopic));
            while (true) {
                ConsumerRecords<String, HubEventAvro> records = kafkaHubEventConsumer.poll(Duration.ofSeconds(4));

                int count = 0;
                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    handleRecord(record);
                    manageOffsets(record, count, kafkaHubEventConsumer);
                    count++;
                }
                kafkaHubEventConsumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.info("Споймано WakeupException в HubEventProcessor {}", ignored.getMessage());
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от хаба", e);
        } finally {

            try {
                kafkaHubEventConsumer.commitSync(currentOffsets);

            } finally {
                log.info("Закрываем хаб консьюмер");
                kafkaHubEventConsumer.close();
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<String, HubEventAvro> record,
                                      int count, KafkaConsumer<String, HubEventAvro> consumer) {
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

    private void handleRecord(ConsumerRecord<String, HubEventAvro> record) {
        hubEventService.handle(record.value());
    }
}
