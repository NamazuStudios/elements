package dev.getelements.elements.app.serve.loader;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import static dev.getelements.elements.app.serve.AppServeConstants.APPLICATION_PREFIX;
import static dev.getelements.elements.app.serve.AppServeConstants.ENABLE_ELEMENTS_AUTH;
import static dev.getelements.elements.common.app.ApplicationElementService.ApplicationElementRecord;
import static org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer.configure;

public class JakartaWebsocketLoader implements Loader {

    private static final Logger logger = LoggerFactory.getLogger(JakartaWebsocketLoader.class);

    public static final String APP_PREFIX_FORMAT = "/app/ws/%s";

    public static final String HANDLER_SEQUENCE = "dev.getelements.elements.app.serve.handler.ws";

    private final Lock lock = new ReentrantLock();

    private final List<DeploymentRecord> activeDeployments = new ArrayList<>();

    private Handler.Sequence sequence;

    private HttpContextRoot httpContextRoot;

    private AuthFilterFeature authFilterFeature;

    @Override
    public void load(final ApplicationElementRecord record, final Element element) {
        try (var mon = Monitor.enter(lock)) {

            final var deployed = activeDeployments
                    .stream()
                    .anyMatch(d -> d.element().equals(element));

            if (deployed) {
                logger.warn("{}/{} is already deployed. Skipping.",
                        record.applicationId(),
                        element.getElementRecord().definition().name());
            } else {

                final var classes = getEndpointClasses(element);

                if (!classes.isEmpty()) {
                    final var deploymentRecord = loadClasses(classes, element);
                    activeDeployments.add(deploymentRecord);
                }

            }

        }
    }

    private ClassGraph newClassGraph(final Element element) {

        final var classgraph = new ClassGraph()
                .enableClassInfo()
                .enableAnnotationInfo()
                .overrideClassLoaders(element.getElementRecord().classLoader());

        final var definition = element.getElementRecord().definition();

        definition.acceptPackages(
                classgraph::acceptPackages,
                classgraph::acceptPackagesNonRecursive
        );

        return classgraph;

    }

    private List<Class<?>> getEndpointClasses(final Element element) {
        try (final var result = newClassGraph(element).scan()) {
            return result
                    .getClassesWithAnnotation(ServerEndpoint.class)
                    .loadClasses();
        }
    }

    private DeploymentRecord loadClasses(final List<Class<?>> classes, final Element element) {

        final var prefix = element
                .getElementRecord()
                .attributes()
                .getAttributeOptional(APPLICATION_PREFIX)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(Predicate.not(String::isBlank))
                .orElseGet(element.getElementRecord().definition()::name);

        final var enableAuth = element
                .getElementRecord()
                .attributes()
                .getAttributeOptional(ENABLE_ELEMENTS_AUTH)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(Boolean::parseBoolean)
                .orElse(false);

        final var contextPath = getHttpContextRoot()
                .formatNormalized(APP_PREFIX_FORMAT, prefix);

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(contextPath);

        if (enableAuth) {
            getAuthFilterFeature().accept(servletContextHandler);
        }

        configure(servletContextHandler, (s, context) -> {
            for (var aClass : classes)
                context.addEndpoint(aClass);
        });

        getSequence().addHandler(servletContextHandler);

        try {
            servletContextHandler.start();
        } catch (Exception e) {
            getSequence().removeHandler(servletContextHandler);
            throw new InternalError(e);
        }

        return new DeploymentRecord(element, servletContextHandler);

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
