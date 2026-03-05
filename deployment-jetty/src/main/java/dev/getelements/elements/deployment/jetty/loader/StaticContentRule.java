package dev.getelements.elements.deployment.jetty.loader;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * A compiled rule parsed from element attributes. Each rule has a regex pattern that is matched against relative file
 * paths and a map of raw header value templates. Template values may contain the variables {@code $filename},
 * {@code $path}, {@code $[0]}, and {@code $[N]} (capture group references).
 *
 * @param name the rule name (used for ordering and diagnostics)
 * @param pattern the compiled regex pattern
 * @param headerTemplates map of header name → raw template value
 */
record StaticContentRule(String name, Pattern pattern, Map<String, String> headerTemplates) {}
