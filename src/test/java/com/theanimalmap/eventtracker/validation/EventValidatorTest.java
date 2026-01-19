package com.theanimalmap.eventtracker.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventValidatorTest {

    private final EventValidator validator = new EventValidator();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void validate_searchAnimal_requiresAnimal() throws Exception {
        JsonNode payload = mapper.readTree("{\"animal\":\"lion\"}");
        assertDoesNotThrow(() -> validator.validate("search_animal", payload));
    }

    @Test
    void validate_searchAnimal_missingAnimal_throws() throws Exception {
        JsonNode payload = mapper.readTree("{}");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("search_animal", payload)
        );

        assertEquals("missing field: animal", ex.getMessage());
    }

    @Test
    void validate_clickCountryFromMap_requiresCountry() throws Exception {
        JsonNode payload = mapper.readTree("{\"country\":\"argentina\"}");
        assertDoesNotThrow(() -> validator.validate("click_country_from_map", payload));
    }

    @Test
    void validate_payloadMustBeObject_throws() throws Exception {
        JsonNode payload = mapper.readTree("[]");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("search_animal", payload)
        );

        assertEquals("payload must be a JSON object", ex.getMessage());
    }

    @Test
    void validate_unknownType_throws() throws Exception {
        JsonNode payload = mapper.readTree("{\"animal\":\"lion\"}");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate("nope", payload)
        );

        assertEquals("unknown type: nope", ex.getMessage());
    }
}