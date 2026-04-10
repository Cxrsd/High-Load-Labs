package org.example.controller;

import org.example.dto.ProcessedMessage;
import org.example.service.ProducerService;
import org.example.service.RedisQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/queue")
public class QueueController {

    private final RedisQueueService queueService;
    private final ProducerService producerService;

    public QueueController(RedisQueueService queueService, ProducerService producerService) {
        this.queueService = queueService;
        this.producerService = producerService;
    }

    @PostMapping("/load-from-file")
    public ResponseEntity<Map<String, String>> loadFromFile() {
        producerService.loadFromFile();
        return ResponseEntity.accepted().body(Map.of(
                "status", "started",
                "message", "messages are loading from data.txt into queue:input"
        ));
    }

    //message cap
    @PostMapping("/demo")
    public ResponseEntity<Map<String, String>> demo(@RequestParam(defaultValue = "10") int count) {
        producerService.loadDemo(count);
        return ResponseEntity.accepted().body(Map.of(
                "status", "started",
                "count", String.valueOf(count),
                "message", "messages are being pushed into queue:input"
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(Map.of(
                "inputQueueSize", queueService.inputQueueSize(),
                "outputQueueSize", queueService.outputQueueSize()
        ));
    }

    @GetMapping("/result/{id}")
    public ResponseEntity<?> getResult(@PathVariable String id) {
        try {
            ProcessedMessage result = queueService.getResult(id);
            if (result == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "error", "Result not found or expired",
                        "id", id
                ));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    //capped
    @GetMapping("/output")
    public ResponseEntity<?> getOutput(@RequestParam(defaultValue = "5") int count) {
        try {
            List<ProcessedMessage> results = queueService.getRecentOutput(count);
            return ResponseEntity.ok(Map.of(
                    "count", results.size(),
                    "messages", results
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
