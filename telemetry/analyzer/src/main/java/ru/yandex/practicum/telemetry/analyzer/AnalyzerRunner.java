package ru.yandex.practicum.telemetry.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.analyzer.service.processor.HubEventProcessor;
import ru.yandex.practicum.telemetry.analyzer.service.processor.SnapshotProcessor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {
    final HubEventProcessor hubEventProcessor;
    final SnapshotProcessor snapshotProcessor;

    @Override
    public void run(String...args) throws Exception {
        Thread hubEventsTread = new Thread(hubEventProcessor);
        hubEventsTread.setName("HubEventHandlerThread");
        hubEventsTread.start();

        snapshotProcessor.start();
    }

}
