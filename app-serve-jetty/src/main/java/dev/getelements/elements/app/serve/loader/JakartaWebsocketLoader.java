package dev.getelements.elements.app.serve.loader;

import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.servlet.HttpContextRoot;
import io.github.classgraph.ClassGraph;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.websocket.server.ServerEndpoint;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import static dev.getelements.elements.app.serve.AppServeConstants.APPLICATION_PREFIX;
import static dev.getelements.elements.app.serve.AppServeConstants.ENABLE_ELEMENTS_AUTH;
import static dev.getelements.elements.sdk.model.Constants.APP_OUTSIDE_URL;
import static org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer.configure;

public class JakartaWebsocketLoader implements Loader {

    private static final Logger logger = LoggerFactory.getLogger(JakartaWebsocketLoader.class);

    public static final String APP_PREFIX_FORMAT = "/app/ws/%s";

    public static final String HANDLER_SEQUENCE = "dev.getelements.elements.app.serve.handler.ws";

    private final Lock lock = new ReentrantLock();

    private final List<JettyDeploymentRecord> activeDeployments = new ArrayList<>();

    private String appOutsideUrl;

    private Handler.Sequence sequence;

    private HttpContextRoot httpContextRoot;

    private AuthFilterFeature authFilterFeature;

    private ClassGraph newClassGraph(final PendingDeployment pending, final Element element) {

        final var classgraph = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .ignoreParentClassLoaders()
                .overrideClassLoaders(element.getElementRecord().classLoader());

        final var definition = element.getElementRecord().definition();

        definition.acceptPackages(
                pkg -> {
                    pending.logf("Scanning Package and Subpackages of %s", pkg);
                    classgraph.acceptPackages(pkg);
                },
                pkg -> {
                    pending.logf("Scanning Package %s", pkg);
                    classgraph.acceptPackagesNonRecursive(pkg);
                }
        );

        return classgraph;

    }

    private List<Class<?>> getEndpointClasses(final PendingDeployment pending, final Element element) {
        try (final var result = newClassGraph(pending, element).scan()) {
            return result
                    .getClassesWithAnnotation(ServerEndpoint.class)
                    .loadClasses();
        }
    }

    private JettyDeploymentRecord loadClasses(final PendingDeployment pending,
                                              final List<Class<?>> classes,
                                              final Element element) {

        pending.logf("Starting JAX-WS deployment for %s.", element.getElementRecord().definition().name());

        final var prefix = element
                .getElementRecord()
                .attributes()
                .getAttributeOptional(APPLICATION_PREFIX)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(Predicate.not(String::isBlank))
                .orElseGet(() -> {

                    pending.logf(
                            "Unable to determine application prefix for %s. Using default.",
                            element.getElementRecord().definition().name()
                    );

                    return element.getElementRecord().definition().name();

                });

        final var enableAuth = element
                .getElementRecord()
                .attributes()
                .getAttributeOptional(ENABLE_ELEMENTS_AUTH)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(Boolean::parseBoolean)
                .orElse(false);

        pending.logf("Built-in Auth Enabled: %s", enableAuth);

        final var contextPath = getHttpContextRoot().formatNormalized(APP_PREFIX_FORMAT, prefix);

        try {

            final var contextPathURI = URI.create(getAppOutsideUrl()).resolve(contextPath);
            final String webSocketScheme = getWebSocketScheme(pending, contextPathURI);

            final var contextPathURIWebSocket = new URI(
                    webSocketScheme,
                    contextPathURI.getUserInfo(),
                    contextPathURI.getHost(),
                    contextPathURI.getPort(),
                    contextPathURI.getPath(),
                    contextPathURI.getQuery(),
                    contextPathURI.getFragment()
            );

            pending.uri(contextPathURIWebSocket);

        } catch (final URISyntaxException ex) {
            pending.warning(ex);
            pending.logWarningf("WARNING! Failed to create WebSocket URI for %s at %s. Check your %s setting.",
                    element.getElementRecord().definition().name(),
                    contextPath,
                    APP_OUTSIDE_URL
            );
        }


        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(contextPath);

        if (enableAuth) {
            getAuthFilterFeature().accept(servletContextHandler);
        }

        configure(servletContextHandler, (s, context) -> {
            for (var aClass : classes) {
                context.addEndpoint(aClass);
                pending.logf("Registered %s as WebSocket endpoint.", aClass.getName());
            }
        });

        getSequence().addHandler(servletContextHandler);

        try {
            servletContextHandler.start();
        } catch (Exception ex) {
            pending.error(ex);
            getSequence().removeHandler(servletContextHandler);
            throw new InternalError(ex);
        }

        return new JettyDeploymentRecord(element, servletContextHandler);

    }

    private static String getWebSocketScheme(final PendingDeployment pending, final URI contextPathURI) {

        final var contextPathURIHost = contextPathURI.getHost();
        final var contextPathURIScheme = contextPathURI.getScheme();
        final var normalizedHost = contextPathURIHost != null ? contextPathURIHost.toLowerCase() : "";

        final boolean isLocalHost =
                normalizedHost.equals("localhost") ||
                normalizedHost.equals("127.0.0.1") ||
                normalizedHost.equals("::1") ||
                normalizedHost.equals("[::1]");

        if (contextPathURIScheme.equalsIgnoreCase("http")) {

            if (!isLocalHost) {
                pending.logWarning(
                        "WARNING! You are serving a WebSocket over an unencrypted connection (http). " +
                        "This is not secure and speaks Cthulhu's true name when used in production."
                );
            }

            return "ws";

        } else if (contextPathURIScheme.equalsIgnoreCase("https")) {
            return "wss";
        } else {

            if (!isLocalHost) {
                pending.logWarningf(
                        "WARNING! You are serving a WebSocket over an unknown connection (%s). " +
                        "This is potentially not secure and speaks Cthulhu's true name when used in production.",
                        contextPathURIScheme
                );
            }

            return "ws";
        }

    }

    @Override
    public void load(final PendingDeployment pending, final RuntimeRecord record, final Element element) {
        try (var mon = Monitor.enter(lock)) {

            final var deployed = activeDeployments
                    .stream()
                    .anyMatch(d -> d.element().equals(element));

            if (deployed) {

                final var appId = record.deployment().id();
                pending.logf("Detected existing deployment for %s.", appId);

                logger.warn("{}/{} is already deployed. Skipping.",
                        appId,
                        element.getElementRecord().definition().name());

            } else {

                final var classes = getEndpointClasses(pending, element);

                if (!classes.isEmpty()) {
                    final var deploymentRecord = loadClasses(pending, classes, element);
                    activeDeployments.add(deploymentRecord);
                    pending.element(element);
                }

            }

        }
    }

    public String getAppOutsideUrl() {
        return appOutsideUrl;
    }

    @Inject
    public void setAppOutsideUrl(@Named(APP_OUTSIDE_URL) String appOutsideUrl) {
        this.appOutsideUrl = appOutsideUrl;
    }

    public Handler.Sequence getSequence() {
        return sequence;
    }

    @Inject
    public void setSequence(@Named(HANDLER_SEQUENCE) Handler.Sequence sequence) {
        this.sequence = sequence;
    }

    public HttpContextRoot getHttpContextRoot() {
        return httpContextRoot;
    }

    @Inject
    public void setHttpContextRoot(HttpContextRoot httpContextRoot) {
        this.httpContextRoot = httpContextRoot;
    }

    public AuthFilterFeature getAuthFilterFeature() {
        return authFilterFeature;
    }

    @Inject
    public void setAuthFilterFeature(AuthFilterFeature authFilterFeature) {
        this.authFilterFeature = authFilterFeature;
    }

}
