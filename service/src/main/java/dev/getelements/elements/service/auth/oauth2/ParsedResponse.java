package dev.getelements.elements.service.auth.oauth2;

import com.fasterxml.jackson.databind.JsonNode;

public record ParsedResponse(
        int status,
        String rawBody,
        JsonNode json
) {}

