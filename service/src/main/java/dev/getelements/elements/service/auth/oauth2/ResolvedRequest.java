package dev.getelements.elements.service.auth.oauth2;

import java.util.Map;

public record ResolvedRequest(
        Map<String, String> headers,
        Map<String, String> queryParams,
        Map<String, String> bodyParams,
        String externalUserIdFromRequest // resolved value if any kv.userId == true
) {}

