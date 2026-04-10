package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.dto.Message;
import org.example.dto.ProcessedMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisQueueService {

    static final String INPUT_QUEUE  = "queue:input";
    static final String OUTPUT_QUEUE = "queue:output";
    static final String RESULT_PREFIX = "result:";
    static final long RESULT_TTL_SECONDS = 300;

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisQueueService(StringRedisTemplate redis) {
        this.redis = redis;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    //producer

    public void pushToInput(Message message) throws JsonProcessingException {
        String json = mapper.writeValueAsString(message);
        redis.opsForList().rightPush(INPUT_QUEUE, json);
    }

    //worker

    public Message popFromInput() throws JsonProcessingException {
        String json = redis.opsForList().leftPop(INPUT_QUEUE);
        if (json == null) return null;
        return mapper.readValue(json, Message.class);
    }

    public void pushToOutput(ProcessedMessage result) throws JsonProcessingException {
        String json = mapper.writeValueAsString(result);
        redis.opsForList().rightPush(OUTPUT_QUEUE, json);
        redis.opsForValue().set(RESULT_PREFIX + result.getId(), json, RESULT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    //HTTP API

    public ProcessedMessage getResult(String id) throws JsonProcessingException {
        String json = redis.opsForValue().get(RESULT_PREFIX + id);
        if (json == null) return null;
        return mapper.readValue(json, ProcessedMessage.class);
    }

    public List<ProcessedMessage> getRecentOutput(int count) throws JsonProcessingException {
        List<String> jsons = redis.opsForList().range(OUTPUT_QUEUE, -count, -1);
        List<ProcessedMessage> results = new ArrayList<>();
        if (jsons == null) return results;
        for (String json : jsons) {
            results.add(mapper.readValue(json, ProcessedMessage.class));
        }
        return results;
    }

    public long inputQueueSize() {
        Long size = redis.opsForList().size(INPUT_QUEUE);
        return size != null ? size : 0;
    }

    public long outputQueueSize() {
        Long size = redis.opsForList().size(OUTPUT_QUEUE);
        return size != null ? size : 0;
    }
}
