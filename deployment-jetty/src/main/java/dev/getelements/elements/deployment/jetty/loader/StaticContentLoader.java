package dev.getelements.elements.deployment.jetty.loader;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.deployment.ElementContainerService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.record.ElementPathRecord;
import dev.getelements.elements.sdk.record.ElementStaticContentRecord;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.servlet.HttpContextRoot;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;

import static dev.getelements.elements.deployment.jetty.loader.HttpPathRegistry.PathConflict;

import static dev.getelements.elements.sdk.model.Constants.APP_OUTSIDE_URL;

/**
 * Loads static content from an {@link ElementPathRecord} and serves it via a {@link ServletContextHandler}.
 *
 * <p>Rule-based header injection and MIME type resolution are handled by {@link StaticRuleEngine} at load time.
 * Serving is handled by {@link StaticContentServlet}.</p>
 */
public abstract class StaticContentLoader implements Loader {

    private static final Logger logger = LoggerFactory.getLogger(StaticContentLoader.class);

    public static final String HANDLER_SEQUENCE = "dev.getelements.elements.app.serve.handler.static";

    private final Function<ElementPathRecord, ElementStaticContentRecord> resolve;

    private final String defaultContextPathFormat;

    private final String uriOverrideAttributeKey;

    private final String ruleNamespace;

    private final Lock lock = new ReentrantLock();

    private final List<JettyDeploymentRecord> activeDeployments = new ArrayList<>();

    private Handler.Sequence sequence;

    private HttpContextRoot httpContextRoot;

    private String appOutsideUrl;

    private HttpPathRegistry httpPathRegistry;

    protected StaticContentLoader(
            final Function<ElementPathRecord, ElementStaticContentRecord> resolve,
            final String defaultContextPathFormat,
            final String uriOverrideAttributeKey,
            final String ruleNamespace) {
        this.resolve = resolve;
        this.defaultContextPathFormat = defaultContextPathFormat;
        this.uriOverrideAttributeKey = uriOverrideAttributeKey;
        this.ruleNamespace = ruleNamespace;
    }

    @Override
    public void load(final PendingDeployment pending,
                     final ElementRuntimeService.RuntimeRecord record,
                     final Element element) {

        try (var mon = Monitor.enter(lock)) {

            final var alreadyDeployed = activeDeployments
                    .stream()
                    .anyMatch(d -> d.element().equals(element));

            if (alreadyDeployed) {

                pending.logWarningf("WARNING: Detected existing static content deployment for %s.", element
                        .getElementRecord()
                        .definition()
                        .name()
                );

                return;

            }

            final var elementPathRecord = record.elementPathsByElement().get(element);
            if (elementPathRecord == null) return;

            final var contentRecord = resolve.apply(elementPathRecord);
            if (contentRecord == null || contentRecord.contents().isEmpty()) {

                pending.logf("No static files found for %s; skipping static content handler.", element
                        .getElementRecord()
                        .definition()
                        .name()
                );

                return;
            }

            final var prefix = element
                    .getElementRecord()
                    .attributes()
                    .getAttributeOptional(ElementContainerService.APPLICATION_PREFIX)
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .filter(Predicate.not(String::isBlank))
                    .orElseGet(() -> {

                        pending.logf("Unable to determine application prefix for %s. Using element name.", element
                                .getElementRecord()
                                .definition()
                                .name()
                        );

                        return element.getElementRecord().definition().name();

                    });

            final var contextPath = element
                    .getElementRecord()
                    .attributes()
                    .getAttributeOptional(uriOverrideAttributeKey)
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .filter(Predicate.not(String::isBlank))
                    .orElseGet(() -> getHttpContextRoot().formatNormalized(defaultContextPathFormat, prefix));

            final var conflict = checkPathConflict(contextPath);

            if (conflict == PathConflict.INNER) {
                pending.logWarningf(
                        "WARNING: Static content path '%s' is inside a reserved system API path. " +
                        "Refusing to load static content for element %s.",
                        contextPath,
                        element.getElementRecord().definition().name()
                );
                return;
            }

            try {
                final var contextPathURI = new URI(getAppOutsideUrl()).resolve(contextPath);
                pending.uri(contextPathURI);
            } catch (final URISyntaxException ex) {
                pending.warning(ex);
                pending.logf("WARNING! Failed to create static content URI for %s at %s. Check your %s setting.",
                        element.getElementRecord().definition().name(),
                        contextPath,
                        APP_OUTSIDE_URL
                );
            }

            final var config = new StaticRuleEngine(
                    contentRecord,
                    element.getElementRecord().attributes(),
                    ruleNamespace,
                    pending
            ).buildIndex();

            final var servlet = new StaticContentServlet(config);
            final var holder = new ServletHolder(servlet);

            final var servletContextHandler = new ServletContextHandler();
            servletContextHandler.setContextPath(contextPath);
            servletContextHandler.setClassLoader(element.getClass().getClassLoader());
            servletContextHandler.addServlet(holder, "/*");

            final var handlerToAdd = conflict == PathConflict.OUTER
                    ? new ReservedPathSkipHandler(servletContextHandler)
                    : servletContextHandler;

            // Register specific paths so other catch-all handlers know to skip them.
            // Catch-all paths (OUTER conflict) are intentionally not registered — registering "/"
            // would make isRegisteredPath() return true for every request.
            if (conflict == PathConflict.NONE) {
                httpPathRegistry.register(contextPath);
            }

            getSequence().addHandler(handlerToAdd);

            try {
                handlerToAdd.start();
            } catch (final Exception ex) {
                if (conflict == PathConflict.NONE) {
                    httpPathRegistry.deregister(contextPath);
                }
                getSequence().removeHandler(handlerToAdd);
                throw new InternalException(ex);
            }

            activeDeployments.add(new JettyDeploymentRecord(element, handlerToAdd));
            pending.element(element);

            pending.logf("Serving %d static file(s) for %s at %s%s.",
                    config.files().size(),
                    element.getElementRecord().definition().name(),
                    contextPath,
                    conflict == PathConflict.OUTER ? " (catch-all; system API paths excluded)" : ""
            );

        }

    }

    @Override
    public void unload(final Element element) {
        try (var mon = Monitor.enter(lock)) {
            final var deployment = activeDeployments.stream()
                    .filter(d -> d.element().equals(element))
                    .findFirst()
                    .orElse(null);

            if (deployment != null) {
                activeDeployments.remove(deployment);

                // Deregister specific paths (catch-all handlers using ReservedPathSkipHandler were never registered)
                if (deployment.handler() instanceof final ServletContextHandler sch) {
                    httpPathRegistry.deregister(sch.getContextPath());
                }

                try {
                    deployment.handler().stop();
                    getSequence().removeHandler(deployment.handler());
                    logger.info("Unloaded static content handler for element: {}",
                            element.getElementRecord().definition().name());
                } catch (final Exception ex) {
                    logger.error("Failed to cleanly unload static content handler for element: {}",
                            element.getElementRecord().definition().name(), ex);
                }
            }
        }
    }

    public Handler.Sequence getSequence() {
        return sequence;
    }

    @Inject
    public void setSequence(@Named(HANDLER_SEQUENCE) final Handler.Sequence sequence) {
        this.sequence = sequence;
    }

    public HttpContextRoot getHttpContextRoot() {
        return httpContextRoot;
    }

    @Inject
    public void setHttpContextRoot(final HttpContextRoot httpContextRoot) {
        this.httpContextRoot = httpContextRoot;
    }

    public String getAppOutsideUrl() {
        return appOutsideUrl;
    }

    @Inject
    public void setAppOutsideUrl(@Named(APP_OUTSIDE_URL) final String appOutsideUrl) {
        this.appOutsideUrl = appOutsideUrl;
    }

    public HttpPathRegistry getHttpPathRegistry() {
        return httpPathRegistry;
    }

    @Inject
    public void setHttpPathRegistry(final HttpPathRegistry httpPathRegistry) {
        this.httpPathRegistry = httpPathRegistry;
    }

    private PathConflict checkPathConflict(final String contextPath) {
        return httpPathRegistry.checkConflict(contextPath);
    }

    /**
     * Wraps a handler and skips it (returning {@code false}) for any request whose path falls under a
     * reserved system API prefix.  This allows a catch-all static content deployment (e.g. context path
     * {@code /}) to coexist with system APIs: reserved paths fall through to subsequent handlers in the
     * {@link Handler.Sequence} while everything else is served as static content.
     */
    private class ReservedPathSkipHandler extends Handler.Wrapper {

        ReservedPathSkipHandler(final Handler handler) {
            super(handler);
        }

        @Override
        public boolean handle(final Request request, final Response response, final Callback callback)
                throws Exception {
            final var path = request.getHttpURI().getPath();
            if (httpPathRegistry.isRegisteredPath(path)) {
                return false;
            }
            return super.handle(request, response, callback);
        }

    }

    /**
     * Loads the UI static content found at {@link dev.getelements.elements.sdk.ElementPathLoader#UI_DIR}.
     * Served under {@code /app/ui/{prefix}} by default.
     */
    public static class UI extends StaticContentLoader {

        public UI() {
            super(ElementPathRecord::uiStaticContent, "/app/ui/%s", ElementContainerService.UI_CONTENT_URI, "ui");
        }

    }

    /**
     * Loads the standard static content found at {@link dev.getelements.elements.sdk.ElementPathLoader#STATIC_DIR}.
     * Served under {@code /app/static/{prefix}} by default.
     */
    public static class Standard extends StaticContentLoader {

        public Standard() {
            super(ElementPathRecord::standardStaticContent, "/app/static/%s", ElementContainerService.STATIC_CONTENT_URI, "static");
        }

    }

}
