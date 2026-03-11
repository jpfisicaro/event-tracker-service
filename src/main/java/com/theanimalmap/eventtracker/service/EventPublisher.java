package com.theanimalmap.eventtracker.service;

public interface EventPublisher {
    void publish(String topic, String eventJson);
}
