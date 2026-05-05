package dev.getelements.elements.deployment.jetty.loader;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.deployment.ElementContainerService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
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
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

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

    /**
     * Tracks in-progress background mount tasks keyed by element.  Entries are present only while
     * {@link Handler#start()} has not yet returned.  Access is guarded by {@link #lock}.
     */
    private final Map<Element, Future<?>> pendingMounts = new HashMap<>();

    /**
     * Daemon thread pool for running the slow Jersey initialisation off the caller's thread.
     * A cached pool is used so that multiple elements can start up in parallel without queuing
     * behind each other.
     */
    private final ExecutorService mountExecutor = Executors.newCachedThreadPool(r -> {
        final var t = new Thread(r, "element-rs-mount");
        t.setDaemon(true);
        return t;
    });

    private final ElementPathResolver pathResolver = new ElementPathResolver();

    private String appOutsideUrl;

    private Sequence sequence;

    private HttpContextRoot httpContextRoot;

    private HttpPathRegistry httpPathRegistry;

    private AuthFilterFeature authFilterFeature;

    /**
     * Captures all context needed to start a Jersey handler on a background thread after
     * {@link #deploy} has already added it to the Jetty handler sequence.
     */
    private record MountContext(
            JettyDeploymentRecord record,
            ClassLoader elementClassLoader,
            Application application,
            String openApiCtxId
    ) {}

    /**
     * Builds the Jetty handler structure for the element's {@link Application} and adds it to the
     * handler sequence.  The handler is intentionally left un-started here; the caller submits
     * {@link #runMountTask} to {@link #mountExecutor} so that the slow Jersey initialisation
     * happens off the calling thread.
     *
     * <p>An un-started {@link ServletContextHandler} in the sequence returns {@code false} from
     * {@link Handler#handle}, so requests 404 gracefully until the background task completes.
     */
    private MountContext deploy(final PendingDeployment pending,
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

        // Force Jersey to initialize eagerly when start() is called on the background thread,
        // not lazily on the first HTTP request.
        holder.setInitOrder(1);

        // Assign a unique OpenAPI context ID for this deployment so that OpenApiResource uses
        // an isolated context rather than inheriting the server-level one (which would expose
        // all Core APIs in the element's /openapi.json response).
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

        // Add to the sequence now (un-started).  Jetty's ContextHandler.handle() checks isStarted();
        // an un-started handler returns false, so requests fall through to a 404 until the
        // background thread calls start().
        getSequence().addHandler(classLoaderHandler);

        return new MountContext(
                new JettyDeploymentRecord(element, classLoaderHandler),
                elementClassLoader,
                application,
                openApiCtxId
        );
    }

    /**
     * Background task that starts the Jetty handler.
     *
     * <p>This is where the cold-start cost lives on first load (Jersey + HK2 wiring, JIT
     * compilation).  Running it off the calling thread means the deployment API returns quickly
     * and the element becomes available once this task completes.
     *
     * <p>After {@link Handler#start()} returns the task re-acquires {@link #lock} to verify the
     * element is still active.  If {@link #unload} fired during startup the element has already
     * been removed from {@link #activeDeployments}; the task then stops/removes the handler
     * itself, avoiding the deadlock that would result from calling stop() while start() is
     * still blocked inside a synchronized method.
     */
    private void runMountTask(final Element element, final MountContext ctx) {

        final var handler = ctx.record().handler();
        final var elementClassLoader = ctx.elementClassLoader();
        final var application = ctx.application();
        final var openApiCtxId = ctx.openApiCtxId();
        final var elementName = element.getElementRecord().definition().name();

        // Set TCCL to the element's classloader for start(): Jersey initialises singletons eagerly
        // (setInitOrder=1); those singletons may trigger MongoDB queries hitting DiscriminatorLookup.
        // The patched DiscriminatorLookup uses TCCL to resolve element-owned @Entity classes that
        // aren't registered via EntityRegistry.
        final var thread = Thread.currentThread();
        final var previousTccl = thread.getContextClassLoader();

        thread.setContextClassLoader(elementClassLoader);

        try {

            // 1. Start the handler (this is the slow part on a cold JVM).
            boolean startSucceeded = false;
            Exception startException = null;
            try {
                handler.start();
                startSucceeded = true;
            } catch (Exception ex) {
                startException = ex;
            }

            // 2. Re-acquire the lock to check whether the element is still active.
            //    unload() may have fired while start() was blocked above.
            boolean stillActive;

            try (var mon = Monitor.enter(lock)) {
                stillActive = activeDeployments.stream().anyMatch(d -> d.element().equals(element));
                pendingMounts.remove(element);
            }

            // 3. Handle failure / post-startup unload.
            if (!startSucceeded) {

                if (stillActive) {
                    logger.error("Failed to start REST handler for element: {}", elementName, startException);
                } else {
                    logger.debug("REST handler startup cancelled for element: {}", elementName);
                }

                getSequence().removeHandler(handler);

                return;
            }

            if (!stillActive) {

                logger.info("REST handler for element {} was unloaded during startup; stopping.", elementName);

                try {
                    handler.stop();
                } catch (Exception ex) {
                    logger.warn("Failed to stop handler for element {} after post-startup unload.", elementName, ex);
                }

                getSequence().removeHandler(handler);

                return;
            }

            // 4. Build the OpenAPI context for this element using the application's own classes
            //    (JaxrsApplicationScanner) and clear the parent context to prevent the server-level
            //    OpenAPI context from being merged in.  This ensures the element's /openapi.json
            //    only contains its own APIs.
            try {

                final var swaggerConfig = new SwaggerConfiguration()
                        .openAPI(new OpenAPI())
                        .scannerClass("io.swagger.v3.jaxrs2.integration.JaxrsApplicationScanner");

                final var oaCtx = new JaxrsOpenApiContextBuilder<>()
                        .ctxId(openApiCtxId)
                        .application(application)
                        .openApiConfiguration(swaggerConfig)
                        .buildContext(true);

                if (oaCtx instanceof GenericOpenApiContext<?> gctx) {
                    gctx.setParent(null);
                }

                oaCtx.read();
                logger.debug("OpenAPI context initialized for {}", elementName);

            } catch (final Exception ex) {
                logger.warn("OpenAPI context initialization failed for {}; /openapi.json may include server-level APIs",
                        elementName, ex);
            }

            logger.info("REST handler ready for element: {}", elementName);

        } finally {
            thread.setContextClassLoader(previousTccl);
        }
    }

    @Override
    public void unload(final Element element) {

        Handler handlerToStop = null;

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

                final var pending = pendingMounts.remove(element);

                if (pending != null) {

                    // Startup is still running on a background thread.  Interrupt it (best-effort)
                    // and let runMountTask() handle stop()/removeHandler() once start() returns.
                    // Calling stop() here while start() is still executing inside a synchronized
                    // method would block for the entire startup duration.
                    pending.cancel(true);

                    logger.info("Cancelled pending startup for element {}; background task will clean up.",
                            element.getElementRecord().definition().name());

                } else {
                    // Startup is complete — safe to stop synchronously (outside the lock below).
                    handlerToStop = deployment.handler();
                }
            }
        }

        if (handlerToStop != null) {

            try {
                handlerToStop.stop();
                getSequence().removeHandler(handlerToStop);
                logger.info("Unloaded REST handler for element: {}",
                        element.getElementRecord().definition().name());
            } catch (Exception ex) {
                logger.error("Failed to cleanly unload REST handler for element: {}",
                        element.getElementRecord().definition().name(), ex);
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
                        .ifPresent(application -> {
                            final var ctx = deploy(pending, element, application);

                            // Add to activeDeployments BEFORE submitting the background task so
                            // that runMountTask's activeDeployments check correctly reflects the
                            // current state even if the executor starts the task immediately.
                            activeDeployments.add(ctx.record());

                            final var future = mountExecutor.submit(() -> runMountTask(element, ctx));
                            pendingMounts.put(element, future);
                            pending.element(element);
                        });
            }

        }
    }

    @Override
    public boolean hasPendingWork(final Element element) {
        try (var mon = Monitor.enter(lock)) {
            return pendingMounts.containsKey(element);
        }
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
