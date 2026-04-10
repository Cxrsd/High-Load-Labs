package org.example.dto;

import java.time.Instant;

public class ProcessedMessage {
    private String id;
    private String payload;
    private String payloadUpper;
    private int payloadLength;
    private boolean processed;
    private Instant createdAt;
    private Instant processedAt;

    public ProcessedMessage() {}

    public ProcessedMessage(Message original) {
        this.id = original.getId();
        this.payload = original.getPayload();
        this.payloadUpper = original.getPayload().toUpperCase();
        this.payloadLength = original.getPayload().length();
        this.processed = true;
        this.createdAt = original.getCreatedAt();
        this.processedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getPayloadUpper() { return payloadUpper; }
    public void setPayloadUpper(String payloadUpper) { this.payloadUpper = payloadUpper; }

    public int getPayloadLength() { return payloadLength; }
    public void setPayloadLength(int payloadLength) { this.payloadLength = payloadLength; }

    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
}
