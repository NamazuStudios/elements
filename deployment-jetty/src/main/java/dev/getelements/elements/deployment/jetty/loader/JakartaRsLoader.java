package dev.getelements.elements.deployment.jetty.loader;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.dao.ElementEntityRegistrar;
import dev.getelements.elements.sdk.deployment.ElementContainerService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.servlet.HttpContextRoot;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.GenericOpenApiContext;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Application;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Handler.Sequence;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.model.Constants.APP_OUTSIDE_URL;
import static org.glassfish.jersey.CommonProperties.MOXY_JSON_FEATURE_DISABLE;
import static org.glassfish.jersey.server.ResourceConfig.forApplication;

/**
 * Searches an {@link Element} for a service of type {@link Application} and loads it into the application container
 * making all types specified in the {@link Application} available as RESTful API Calls.
 */
public class JakartaRsLoader implements Loader {

    private static final Logger logger = LoggerFactory.getLogger(JakartaRsLoader.class);

    public static final String HANDLER_SEQUENCE = "dev.getelements.elements.app.serve.handler.rs";

    private final Lock lock = new ReentrantLock();

    private final List<JettyDeploymentRecord> activeDeployments = new ArrayList<>();

    private final ElementPathResolver pathResolver = new ElementPathResolver();

    private String appOutsideUrl;

    private Sequence sequence;

    private HttpContextRoot httpContextRoot;

    private HttpPathRegistry httpPathRegistry;

    private AuthFilterFeature authFilterFeature;

    private ElementEntityRegistrar elementEntityRegistrar;

    private JettyDeploymentRecord deploy(final PendingDeployment pending,
                                         final Element element,
                                         final Application application) {

        pending.logf(
                "Starting REST deployment for %s.",
                element.getElementRecord().definition().name()
        );

        final var enableAuth = element
                .getElementRecord()
                .attributes()
                .getAttributeOptional(ElementContainerService.ENABLE_ELEMENTS_AUTH)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(Boolean::parseBoolean)
                .orElse(false);

        pending.logf("Built-in Auth Enabled: %s", enableAuth);

        final var contextPath = pathResolver.resolveRsContextPath(element, getHttpContextRoot(), pending);

        if (!httpPathRegistry.register(contextPath)) {
            pending.logWarningf(
                    "WARNING: REST path '%s' is already registered by another element or the system. " +
                    "Element %s will still be deployed but may conflict.",
                    contextPath,
                    element.getElementRecord().definition().name()
            );
        }

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

        final var config = forApplication(application)
                .register(OpenApiResource.class)
                .register(new ElementBinder(element));

        if (!config.hasProperty(MOXY_JSON_FEATURE_DISABLE)) {
            // We know this interferes with the user-supplied OAS specification so we eliminate it if the application
            // doesn't specify one way or another. We assume the application provides its own support for JSON or
            // whatever media types it wants.
            config.property(MOXY_JSON_FEATURE_DISABLE, true);
        }

        final var container = new ServletContainer(config);
        final var holder = new ServletHolder(container);

        // Force Jersey to initialize when the ServletContextHandler starts (servletContextHandler.start()
        // below), not lazily on the first HTTP request. Without this, Jersey defers scanning JAX-RS
        // resource classes and instantiating singletons to the first request, causing a multi-second
        // hang — especially for Kotlin elements where kotlin-reflect initialization is expensive.
        holder.setInitOrder(1);

        // Assign a unique OpenAPI context ID for this deployment.  OpenApiResource reads this init
        // parameter to look up its OpenApiContext from OpenApiContextLocator.  By using the same ID
        // in the pre-warm call below we ensure both the warm-up and the first HTTP request share
        // the same pre-built context.  A UUID prevents stale contexts from being reused on reload.
        final var openApiCtxId = UUID.randomUUID().toString();
        holder.setInitParameter("openapi.context.id", openApiCtxId);

        final var elementClassLoader = application.getClass().getClassLoader();

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(contextPath);
        servletContextHandler.setClassLoader(elementClassLoader);
        servletContextHandler.addServlet(holder, "/*");

        if (enableAuth) {
            getAuthFilterFeature().accept(servletContextHandler);
        }

        // Wrap in ClassLoaderSwitchHandler so the element's classloader is the TCCL for the
        // entire duration of each request.  This covers Morphia DiscriminatorLookup fallbacks
        // and any other library that resolves classes via Thread.currentThread().getContextClassLoader().
        final var classLoaderHandler = new ClassLoaderSwitchHandler(elementClassLoader, servletContextHandler);
        getSequence().addHandler(classLoaderHandler);

        // Pre-register entity classes with Morphia before the servlet context starts accepting
        // requests.  This eliminates the DiscriminatorLookup fallback to Class.forName() (which
        // uses Morphia's own classloader and cannot see element-specific classes).
        if (getElementEntityRegistrar() != null) {
            getElementEntityRegistrar().registerEntityClasses(element);
        }

        // Set TCCL to the element's classloader for both start() and the OpenAPI pre-warm below.
        //
        // (1) start(): Jersey initialises singletons eagerly (setInitOrder=1); those singletons
        //     may trigger MongoDB queries hitting DiscriminatorLookup.  The patched
        //     DiscriminatorLookup uses TCCL to resolve element-owned @Entity classes that aren't
        //     registered via MorphiaEntityRegistry.
        //
        // (2) OpenAPI pre-warm: Swagger's annotation scanner triggers kotlin-reflect and other
        //     per-JVM initialisation the first time it introspects a Kotlin class.  Running the
        //     scan here, with the correct TCCL, means the first real HTTP request to /openapi.json
        //     finds the spec already built and returns immediately.
        final var thread = Thread.currentThread();
        final var previousTccl = thread.getContextClassLoader();
        thread.setContextClassLoader(elementClassLoader);
        try {
            try {
                classLoaderHandler.start();
            } catch (Exception ex) {
                getSequence().removeHandler(classLoaderHandler);
                throw new InternalException(ex);
            }

            // Pre-warm the OpenAPI spec.  buildContext(true) initialises Swagger's reader/scanner;
            // read() runs the full annotation scan and caches the result in the OpenApiContext.
            // The context is stored in OpenApiContextLocator under openApiCtxId so that
            // OpenApiResource picks it up (via "openapi.context.id" init param) without re-scanning.
            final var elementName = element.getElementRecord().definition().name();
            try {
                // JaxrsAnnotationScanner.classes() always runs ClassGraph.scan(). With no
                // resourcePackages whitelist the scan covers the entire classpath, finding
                // every @Path class including the platform's Core API endpoints.
                //
                // Fix: derive resourcePackages from the application's own classes so
                // ClassGraph whitelists only those packages.  We also pass the Application
                // object directly (.application()) so that Class objects already loaded by
                // the element's classloader are used as-is — no Class.forName() call is
                // made, avoiding ClassNotFoundException for element-isolated types.
                // (The resourceClasses string approach triggers an early-return path in
                // JaxrsAnnotationScanner that calls Class.forName(name) with the system
                // classloader, which cannot see element-isolated classes.)
                final Set<String> resourcePackages = Stream.concat(
                        application.getClasses().stream(),
                        application.getSingletons().stream().map(Object::getClass)
                ).map(cls -> cls.getPackage().getName()).collect(Collectors.toSet());

                final var swaggerConfig = new SwaggerConfiguration()
                        .openAPI(new OpenAPI())
                        .resourcePackages(resourcePackages);

                final var ctx = new JaxrsOpenApiContextBuilder<>()
                        .ctxId(openApiCtxId)
                        .application(application)
                        .openApiConfiguration(swaggerConfig)
                        .buildContext(true);

                // Also clear any parent context to prevent the server's default OpenAPI
                // context from being merged in as a base spec.
                if (ctx instanceof GenericOpenApiContext<?> gctx) {
                    gctx.setParent(null);
                }

                ctx.read();
                logger.debug("OpenAPI spec pre-warmed for {}", elementName);
            } catch (final Exception ex) {
                logger.warn("OpenAPI pre-warm failed for {}; first /openapi.json request will be slow",
                        elementName, ex);
            }
        } finally {
            thread.setContextClassLoader(previousTccl);
        }

        return new JettyDeploymentRecord(element, classLoaderHandler);

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

                final var sch = findServletContextHandler(deployment.handler());
                if (sch != null) {
                    httpPathRegistry.deregister(sch.getContextPath());
                }

                try {
                    deployment.handler().stop();
                    getSequence().removeHandler(deployment.handler());
                    logger.info("Unloaded REST handler for element: {}",
                            element.getElementRecord().definition().name());
                } catch (Exception ex) {
                    logger.error("Failed to cleanly unload REST handler for element: {}",
                            element.getElementRecord().definition().name(), ex);
                }
            }
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

    public ElementEntityRegistrar getElementEntityRegistrar() {
        return elementEntityRegistrar;
    }

    @com.google.inject.Inject(optional = true)
    public void setElementEntityRegistrar(final ElementEntityRegistrar elementEntityRegistrar) {
        this.elementEntityRegistrar = elementEntityRegistrar;
    }

    /** Traverses a {@link Handler.Wrapper} chain to find the first {@link ServletContextHandler}. */
    private static ServletContextHandler findServletContextHandler(final Handler handler) {
        if (handler instanceof final ServletContextHandler sch) return sch;
        if (handler instanceof final Handler.Wrapper w) return findServletContextHandler(w.getHandler());
        return null;
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

    public HttpPathRegistry getHttpPathRegistry() {
        return httpPathRegistry;
    }

    @Inject
    public void setHttpPathRegistry(HttpPathRegistry httpPathRegistry) {
        this.httpPathRegistry = httpPathRegistry;
    }

    public AuthFilterFeature getAuthFilterFeature() {
        return authFilterFeature;
    }

    @Inject
    public void setAuthFilterFeature(AuthFilterFeature authFilterFeature) {
        this.authFilterFeature = authFilterFeature;
    }

}
