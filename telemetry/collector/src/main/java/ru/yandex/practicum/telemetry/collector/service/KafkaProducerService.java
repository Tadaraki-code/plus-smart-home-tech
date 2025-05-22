package ru.yandex.practicum.telemetry.collector.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducerService {
    private final KafkaProducer<String, SpecificRecordBase> kafkaProducer;

    public KafkaProducerService(KafkaProducer<String, SpecificRecordBase> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    public void sendToKafka(String topic, String key, SpecificRecordBase event) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, key, event);
        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.info("Возникла ошибка при отправки сообщения топик {}: {}", topic, exception.getMessage());
            } else {
                log.info("Сообщение успешно отправлено в топик {}", topic);
            }
        });
    }

    @PreDestroy
    public void close() {
        kafkaProducer.flush();
        kafkaProducer.close();
    }
}
