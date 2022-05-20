package com.namazustudios.socialengine.docserve;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.docserve.guice.DocJerseyModule;
import com.namazustudios.socialengine.docserve.guice.LuaStaticPathDocsModule;
import com.namazustudios.socialengine.rest.guice.RestAPISecurityModule;
import com.namazustudios.socialengine.rest.guice.RestAPIServicesModule;
import com.namazustudios.socialengine.service.guice.RedissonServicesModule;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.DispatcherType;
import java.util.HashMap;
import java.util.List;

import static com.namazustudios.socialengine.Constants.HTTP_PATH_PREFIX;
import static com.namazustudios.socialengine.docserve.DocGuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME;
import static java.lang.String.format;
import static java.util.EnumSet.allOf;
import static org.eclipse.jetty.util.Loader.getResource;

public class DocAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final Logger logger = LoggerFactory.getLogger(DocAppProvider.class);

    public static final String LUA_CONTEXT_FORMAT = "%s/lua";

    public static final String REST_CONTEXT_FORMAT = "%s/rest";

    public static final String SWAGGER_CONTEXT_FORMAT = "%s/swagger";

    private String pathPrefix;

    private String luaContext;

    private String restContext;

    private String swaggerContext;

    private Injector injector;

    private DeploymentManager deploymentManager;

    @Override
    protected void doStart() {
        List.of(
            new App(getDeploymentManager(), this, getLuaContext()),
            new App(getDeploymentManager(), this, getRestContext()),
            new App(getDeploymentManager(), this, getSwaggerContext())
        ).forEach(getDeploymentManager()::addApp);
    }

    @Override
    public ContextHandler createContextHandler(final App app) throws Exception {

        final var originId = app.getOriginId();

        if (originId.equals(getLuaContext())) {
            return createLuaContext(app);
        } else if (originId.equals(getRestContext())) {
            return createRestContext(app);
        } else if (originId.equals(getSwaggerContext())) {
            return createSwaggerContext(app);
        }

        throw new IllegalStateException("Unknown App: " + app.getOriginId());

    }

    private ContextHandler createLuaContext(final App app) {

        if (!getLuaContext().equals(app.getOriginId())) {
            throw new IllegalStateException("Expected: " + getLuaContext());
        }

        final var injector = getInjector().createChildInjector(new LuaStaticPathDocsModule());
        final var pathDocs = injector.getInstance(StaticPathDocs.class);
        pathDocs.start();

        final var defaultServlet = new DefaultServlet();
        final var defaultServletHolder = new ServletHolder("default", defaultServlet);

        final var resourceBase = pathDocs.getPath().toAbsolutePath().toString();
        defaultServletHolder.setInitParameter("resourceBase", resourceBase);
        logger.info("Serving documentation at path {}", resourceBase);

        final var servletContextHandler = new ServletContextHandler();

        servletContextHandler.addServlet(defaultServletHolder, "/*");
        servletContextHandler.setContextPath(getLuaContext());
        servletContextHandler
            .getMimeTypes()
            .addMimeMapping("lua", "text/plain");

        return servletContextHandler;

    }

    private ContextHandler createRestContext(final App app) {

        if (!getRestContext().equals(app.getOriginId())) {
            throw new IllegalStateException("Expected: " + getRestContext());
        }

        final var injector = getInjector().createChildInjector(
            new DocJerseyModule(),
            new RestAPISecurityModule(),
            new RestAPIServicesModule(),
            new RedissonServicesModule(ServletScopes.REQUEST)
        );

        final var servletContextHandler = new ServletContextHandler();

        final var guiceFilter = injector.getInstance(GuiceFilter.class);
        servletContextHandler.getServletContext().setAttribute(INJECTOR_ATTRIBUTE_NAME, injector);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));
        servletContextHandler.setContextPath(getRestContext());

        return servletContextHandler;

    }

    private ContextHandler createSwaggerContext(final App app) {

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(getSwaggerContext());

        final var redirect = injector.createChildInjector().getInstance(DocRedirectFilter.class);
        servletContextHandler.addFilter(new FilterHolder(redirect), "/*", allOf(DispatcherType.class));

        final var defaultServletHolder = servletContextHandler.addServlet(DefaultServlet.class, "/*");

        final var defaultInitParameters = new HashMap<String, String>();
        defaultInitParameters.put("dirAllowed", "false");
        defaultInitParameters.put("resourceBase", getResource("swagger").toString());
        defaultServletHolder.setInitParameters(defaultInitParameters);

        return servletContextHandler;

    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public String getLuaContext() {
        return luaContext;
    }

    public String getRestContext() {
        return restContext;
    }

    public String getSwaggerContext() {
        return swaggerContext;
    }

    @Inject
    public void setPathPrefix(@Named(HTTP_PATH_PREFIX) final String pathPrefix) {

        this.pathPrefix = pathPrefix;

        this.luaContext = format(LUA_CONTEXT_FORMAT, getPathPrefix());
        this.restContext = format(REST_CONTEXT_FORMAT, getPathPrefix());
        this.swaggerContext = format(SWAGGER_CONTEXT_FORMAT, getPathPrefix());

        this.luaContext = this.luaContext.startsWith("/")
            ? this.luaContext
            : "/" + this.luaContext;

        this.restContext = this.restContext.startsWith("/")
            ? this.restContext
            : "/" + this.restContext;

        this.swaggerContext = this.swaggerContext.startsWith("/")
            ? this.swaggerContext
            : "/" + this.swaggerContext;

    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Override
    public void setDeploymentManager(final DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

}
