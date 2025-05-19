package dev.getelements.elements.app.serve.loader;

import dev.getelements.elements.app.serve.AppServeConstants;
import dev.getelements.elements.common.app.ApplicationElementService.ApplicationElementRecord;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.servlet.HttpContextRoot;
import dev.getelements.elements.servlet.security.HttpServletAuthenticationFilter;
import dev.getelements.elements.servlet.security.HttpServletElementScopeFilter;
import dev.getelements.elements.servlet.security.HttpServletHeaderProfileOverrideFilter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.servlet.DispatcherType;
import jakarta.ws.rs.core.Application;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Handler.Sequence;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.EnumSet.allOf;
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

    private final List<DeploymentRecord> activeDeployments = new ArrayList<>();

    private Sequence sequence;

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
                element.getServiceLocator()
                        .findInstance(Application.class)
                        .map(Supplier::get)
                        .filter(a -> Application.class != a.getClass())
                        .filter(a -> !a.getClasses().isEmpty() || !a.getSingletons().isEmpty())
                        .map(a -> deploy(element, a))
                        .ifPresent(activeDeployments::add);
            }

        }
    }

    private DeploymentRecord deploy(final Element element, final Application application) {

        // *bruh*
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

        final var config = forApplication(application);
        final var container = new ServletContainer(config);
        final var holder = new ServletHolder(container);

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

        return new DeploymentRecord(element, servletContextHandler);

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
