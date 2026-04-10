package org.example.worker;

import org.example.dto.Message;
import org.example.dto.ProcessedMessage;
import org.example.service.RedisQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class QueueWorker {

    private static final Logger log = LoggerFactory.getLogger(QueueWorker.class);
    private static final int POLL_INTERVAL_MS = 200;

    private final RedisQueueService queueService;

    public QueueWorker(RedisQueueService queueService) {
        this.queueService = queueService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        Thread worker = new Thread(() -> {
            log.info("Worker started, polling queue:input");
            while (true) {
                try {
                    Message message = queueService.popFromInput();
                    if (message != null) {
                        ProcessedMessage result = process(message);
                        queueService.pushToOutput(result);
                        log.info("Processed id={} payload=\"{}\"", result.getId(), result.getPayload());
                    } else {
                        Thread.sleep(POLL_INTERVAL_MS);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Worker interrupted, stopping.");
                    break;
                } catch (Exception e) {
                    log.error("Worker error", e);
                }
            }
        });
        worker.setName("queue-worker");
        worker.setDaemon(true);
        worker.start();
    }

    private ProcessedMessage process(Message message) {
        return new ProcessedMessage(message);
    }
}
