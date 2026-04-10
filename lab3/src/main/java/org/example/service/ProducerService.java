package org.example.service;

import org.example.dto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ProducerService {

    private static final Logger log = LoggerFactory.getLogger(ProducerService.class);
    private static final int MIN_DELAY_MS = 100;
    private static final int MAX_DELAY_MS = 1000;

    private final RedisQueueService queueService;

    public ProducerService(RedisQueueService queueService) {
        this.queueService = queueService;
    }

    public void loadFromFile() {
        Thread t = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new ClassPathResource("data.txt").getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) continue;

                    Message message = new Message(
                            UUID.randomUUID().toString(),
                            trimmed,
                            Instant.now()
                    );
                    queueService.pushToInput(message);
                    log.info("Pushed message id={} payload=\"{}\"", message.getId(), trimmed);

                    int delay = ThreadLocalRandom.current().nextInt(MIN_DELAY_MS, MAX_DELAY_MS + 1);
                    Thread.sleep(delay);
                }
                log.info("File loading complete.");
            } catch (Exception e) {
                log.error("Error loading from file", e);
            }
        });
        t.setName("producer");
        t.start();
    }

    //fallback
    public void loadDemo(int count) {
        Thread t = new Thread(() -> {
            try {
                for (int i = 1; i <= count; i++) {
                    Message message = new Message(
                            UUID.randomUUID().toString(),
                            "message #" + i,
                            Instant.now()
                    );
                    queueService.pushToInput(message);
                    log.info("pushed id={}", message.getId());
                    Thread.sleep(50);
                }
                log.info("loading complete ({} messages).", count);
            } catch (Exception e) {
                log.error("Error in demo producer", e);
            }
        });
        t.setName("producer-demo");
        t.start();
    }
}
