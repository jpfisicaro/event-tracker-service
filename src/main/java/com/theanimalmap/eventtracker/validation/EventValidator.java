package com.theanimalmap.eventtracker.validation;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

@Component
public class EventValidator {

    public void validate(String type, JsonNode payload) {
        if (payload == null || payload.isNull() || !payload.isObject()) {
            throw new IllegalArgumentException("payload must be a JSON object");
        }

        switch (type) {
            case "search_animal", "click_animal_from_country", "click_animal_from_directory" -> require(payload, "animal");
            case "search_country", "click_country_from_map", "click_country_from_directory",
                 "click_country_from_animal", "click_country_from_country" -> require(payload, "country");
            default -> throw new IllegalArgumentException("unknown type: " + type);
        }
    }

    private void require(JsonNode payload, String field) {
        JsonNode node = payload.get(field);
        if (node == null || node.isNull() || node.asText().isBlank()) {
            throw new IllegalArgumentException("missing field: " + field);
        }
    }
}