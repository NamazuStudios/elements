package dev.getelements.elements.app.serve.loader;

import dev.getelements.elements.app.serve.AppServeConstants;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.servlet.HttpContextRoot;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Application;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Handler.Sequence;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static dev.getelements.elements.sdk.model.Constants.APP_OUTSIDE_URL;
import static org.glassfish.jersey.server.ResourceConfig.forApplication;

/**
 * Searches an {@link Element} for a service of type {@link Application} and loads it into the application container
 * making all types specified in the {@link Application} available as RESTful API Calls.
 */
public class JakartaRsLoader implements AppServeConstants, Loader {

    private static final Logger logger = LoggerFactory.getLogger(JakartaRsLoader.class);

    public static final String APP_PREFIX_FORMAT = "/app/rest/%s";

    public static final String HANDLER_SEQUENCE = "dev.getelements.elements.app.serve.handler.rs";

    private final Lock lock = new ReentrantLock();

    private final List<JettyDeploymentRecord> activeDeployments = new ArrayList<>();

    private String appOutsideUrl;

    private Sequence sequence;

    private HttpContextRoot httpContextRoot;

    private AuthFilterFeature authFilterFeature;

    private JettyDeploymentRecord deploy(final PendingDeployment pending,
                                         final Element element,
                                         final Application application) {

        pending.logf(
                "Starting REST deployment for %s.",
                element.getElementRecord().definition().name()
        );

        // *bruh*
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
            final var contextPathURI = new URI(getAppOutsideUrl()).resolve(contextPath);
            pending.uri(contextPathURI);
        } catch (final URISyntaxException ex) {
            pending.warning(ex);
            pending.logf("WARNING! Failed to create WebSocket URI for %s at %s. Check your %s setting.",
                    element.getElementRecord().definition().name(),
                    contextPath,
                    APP_OUTSIDE_URL
            );
        }

        final var config = forApplication(application).register(OpenApiResource.class);
        final var container = new ServletContainer(config);
        final var holder = new ServletHolder(container);

        holder.setInitParameter(
                "openApi.configuration.resourceClasses",
                "io.swagger.v3.jaxrs2.integration.resources.OpenApiResource"
        );

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(contextPath);
        servletContextHandler.addServlet(holder, "/*");

        if (enableAuth) {
            getAuthFilterFeature().accept(servletContextHandler);
        }

        getSequence().addHandler(servletContextHandler);

        try {
            servletContextHandler.start();
        } catch (Exception ex) {
            getSequence().removeHandler(servletContextHandler);
            throw new InternalException(ex);
        }

        return new JettyDeploymentRecord(element, servletContextHandler);

    }

    @Override
    public void load(final PendingDeployment pending, final RuntimeRecord record, final Element element) {
        try (var mon = Monitor.enter(lock)) {

            final var deployed = activeDeployments
                    .stream()
                    .anyMatch(d -> d.element().equals(element));

            if (deployed) {
                final var appId = record.deployment().id();
                pending.logWarningf("WARNING: Detected existing deployment for %s.", appId);
                logger.warn("{}/{} is already deployed. Skipping.",
                        appId,
                        element.getElementRecord().definition().name());
            } else {
                element.getServiceLocator()
                        .findInstance(Application.class)
                        .map(Supplier::get)
                        .filter(a -> Application.class != a.getClass())
                        .filter(a -> !a.getClasses().isEmpty() || !a.getSingletons().isEmpty())
                        .map(a -> deploy(pending, element, a))
                        .ifPresent(deploymentRecord -> {
                            activeDeployments.add(deploymentRecord);
                            pending.element(element);
                        });
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

    public Sequence getSequence() {
        return sequence;
    }

    @Inject
    public void setSequence(@Named(HANDLER_SEQUENCE) Sequence sequence) {
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
