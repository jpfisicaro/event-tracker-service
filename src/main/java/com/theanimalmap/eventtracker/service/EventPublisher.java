package com.theanimalmap.eventtracker.service;

import com.theanimalmap.eventtracker.dto.EventRequest;

public interface EventPublisher {
    void publish(EventRequest event);
}
