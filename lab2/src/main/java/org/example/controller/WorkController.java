package org.example.controller;

import org.example.model.HealthResponse;
import org.example.model.WorkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;

@RestController
public class WorkController {

    private static final Logger log = LoggerFactory.getLogger(WorkController.class);
    private static final String COUNTER_KEY = "request:counter";

    private final StringRedisTemplate redisTemplate;
    // fallback
    private final AtomicLong localCounter = new AtomicLong(0);

    @Value("${instance.id}")
    private String instance;

    public WorkController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("ok", instance);
    }

    @GetMapping("/")
    public WorkResponse index(@RequestParam(defaultValue = "simple") String mode)
            throws InterruptedException {
        long count;
        try {
            Long redisCount = redisTemplate.opsForValue().increment(COUNTER_KEY);
            count = redisCount != null ? redisCount : localCounter.incrementAndGet();
        } catch (Exception e) {
            log.error("Redis unavailable, falling back to local counter: {}", e.getMessage());
            count = localCounter.incrementAndGet();
        }

        long start = System.nanoTime();

        if ("mixed".equals(mode) && count % 3 == 0) {
            TimeUnit.MILLISECONDS.sleep(300);
        }

        double elapsedMs = (System.nanoTime() - start) / 1_000_000.0;
        return new WorkResponse(count, instance, Math.round(elapsedMs * 100.0) / 100.0);
    }
}
