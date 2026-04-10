package org.example.dto;

import java.time.Instant;

public class Message {
    private String id;
    private String payload;
    private Instant createdAt;

    public Message() {}

    public Message(String id, String payload, Instant createdAt) {
        this.id = id;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
