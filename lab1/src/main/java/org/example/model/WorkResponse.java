package org.example.model;

public record WorkResponse(
        long counter,
        String instance,
        double elapsedMs
) {}
