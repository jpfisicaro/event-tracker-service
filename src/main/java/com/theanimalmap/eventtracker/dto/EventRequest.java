package com.theanimalmap.eventtracker.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequest(
        @Schema(example = "search_animal")
        @NotBlank String type,
        @Schema(example = "{\"animal\": \"badger\"}")
        @NotNull JsonNode payload
) {
}