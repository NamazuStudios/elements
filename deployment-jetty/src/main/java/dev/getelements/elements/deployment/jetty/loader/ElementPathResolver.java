package dev.getelements.elements.deployment.jetty.loader;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.servlet.HttpContextRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

import static dev.getelements.elements.sdk.deployment.ElementContainerService.APPLICATION_PREFIX;
import static dev.getelements.elements.sdk.deployment.ElementContainerService.RS_ROOT;
import static dev.getelements.elements.sdk.deployment.ElementContainerService.WS_ROOT;

/**
 * Centralises the path resolution logic for Jakarta RS and WebSocket loader mount points.
 *
 * <h2>Priority rules</h2>
 * <ol>
 *   <li>If {@code dev.getelements.elements.app.serve.prefix} ({@link dev.getelements.elements.sdk.deployment.ElementContainerService#APPLICATION_PREFIX})
 *       is present and non-blank, the legacy computed paths are used:
 *       {@code /app/rest/<prefix>} and {@code /app/ws/<prefix>}.
 *       A deprecation warning is emitted to the deployment log.</li>
 *   <li>Otherwise, {@link dev.getelements.elements.sdk.deployment.ElementContainerService#RS_ROOT} and
 *       {@link dev.getelements.elements.sdk.deployment.ElementContainerService#WS_ROOT} are used as
 *       absolute paths (relative to the Elements HTTP root).</li>
 *   <li>If neither set of attributes is present the element name is used with the legacy prefix format
 *       as a default fallback.</li>
 * </ol>
 */
public class ElementPathResolver {

    private static final Logger logger = LoggerFactory.getLogger(ElementPathResolver.class);

    static final String LEGACY_RS_FORMAT = "/app/rest/%s";

    static final String LEGACY_WS_FORMAT = "/app/ws/%s";

    /**
     * Resolves the Jakarta RS context path for the given element.
     *
     * @param element         the element being deployed
     * @param httpContextRoot the HTTP context root used to normalise paths
     * @param pending         the pending deployment for logging
     * @return the resolved, normalised context path
     */
    public String resolveRsContextPath(
            final Element element,
            final HttpContextRoot httpContextRoot,
            final Loader.PendingDeployment pending) {
        return resolve(element, httpContextRoot, pending, RS_ROOT, LEGACY_RS_FORMAT);
    }

    /**
     * Resolves the Jakarta WebSocket context path for the given element.
     *
     * @param element         the element being deployed
     * @param httpContextRoot the HTTP context root used to normalise paths
     * @param pending         the pending deployment for logging
     * @return the resolved, normalised context path
     */
    public String resolveWsContextPath(
            final Element element,
            final HttpContextRoot httpContextRoot,
            final Loader.PendingDeployment pending) {
        return resolve(element, httpContextRoot, pending, WS_ROOT, LEGACY_WS_FORMAT);
    }

    private String resolve(
            final Element element,
            final HttpContextRoot httpContextRoot,
            final Loader.PendingDeployment pending,
            final String rootAttributeKey,
            final String legacyFormat) {

        final var attrs = element.getElementRecord().attributes();
        final var elementName = element.getElementRecord().definition().name();

        final var legacyPrefix = attrs.getAttributeOptional(APPLICATION_PREFIX)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(Predicate.not(String::isBlank))
                .orElse(null);

        if (legacyPrefix != null) {
            final var msg = "DEPRECATION: Element '%s' uses legacy attribute '%s'. " +
                    "Migrate to '%s' for explicit path control.";
            pending.logWarningf(msg, elementName, APPLICATION_PREFIX, rootAttributeKey);
            logger.warn(msg.formatted(elementName, APPLICATION_PREFIX, rootAttributeKey));
            return httpContextRoot.formatNormalized(legacyFormat, legacyPrefix);
        }

        return attrs.getAttributeOptional(rootAttributeKey)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(Predicate.not(String::isBlank))
                .map(httpContextRoot::normalize)
                .orElseGet(() -> {
                    pending.logf("No path attribute found for %s; using element name as default.", elementName);
                    return httpContextRoot.formatNormalized(legacyFormat, elementName);
                });
    }

}
