package ru.yandex.practicum.telemetry.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.aggregator.service.AggregatorService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<String, SensorEventAvro> kafkaConsumer;
    private final KafkaProducer<String, SpecificRecordBase> kafkaProducer;
    private final AggregatorService aggregatorService;
    private final String snapshotTopic;
    private final String sensorTopic;

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaConsumer::wakeup));
        try {
            kafkaConsumer.subscribe(List.of(sensorTopic));
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = kafkaConsumer.poll(Duration.ofSeconds(4));

                int count = 0;
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    handleRecord(record);
                    manageOffsets(record, count, kafkaConsumer);
                    count++;
                }
                kafkaConsumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.info("Споймано WakeupException в aggregator {}", ignored.getMessage());
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {

            try {
                kafkaProducer.flush();
                kafkaConsumer.commitSync(currentOffsets);

            } finally {
                log.info("Закрываем консьюмер");
                kafkaConsumer.close();
                log.info("Закрываем продюсер");
                kafkaProducer.close();
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<String, SensorEventAvro> record,
                                      int count, KafkaConsumer<String, SensorEventAvro> consumer) {
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

    private void handleRecord(ConsumerRecord<String, SensorEventAvro> record) {
        log.info("топик = {}, партиция = {}, смещение = {}, значение: {}\n",
                record.topic(), record.partition(), record.offset(), record.value());
        Optional<SensorsSnapshotAvro> snapshot = aggregatorService.updateState(record.value());
        if (snapshot.isPresent()) {
            SensorsSnapshotAvro snapshotAvro = snapshot.get();
            ProducerRecord<String, SpecificRecordBase> message = new ProducerRecord<>(snapshotTopic,
                    snapshotAvro.getHubId(), snapshotAvro);
            kafkaProducer.send(message, (metadata, exception) -> {
                if (exception != null) {
                    log.info("Возникла ошибка при отправки сообщения в топик {}: {}", snapshotTopic,
                            exception.getMessage());
                } else {
                    log.info("Сообщение успешно отправлено в топик {}", snapshotTopic);
                }
            });
        }
    }
}
