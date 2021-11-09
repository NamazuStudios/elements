package com.namazustudios.socialengine.docserve;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.docserve.guice.DocJerseyModule;
import com.namazustudios.socialengine.docserve.guice.LuaStaticPathDocsModule;
import com.namazustudios.socialengine.rest.guice.RestAPISecurityModule;
import com.namazustudios.socialengine.rest.guice.RestAPIServicesModule;
import com.namazustudios.socialengine.service.guice.RedissonServicesModule;
import com.namazustudios.socialengine.servlet.security.HealthServlet;
import com.namazustudios.socialengine.servlet.security.VersionServlet;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.DispatcherType;
import java.util.List;

import static com.namazustudios.socialengine.Constants.HTTP_PATH_PREFIX;
import static java.lang.String.format;
import static java.util.EnumSet.allOf;

public class DocAppProvider extends AbstractLifeCycle implements AppProvider {

    public static final String LUA_CONTEXT_FORMAT = "%s/lua";

    public static final String HEALTH_CONTEXT_FORMAT = "%s/health";

    public static final String VERSION_CONTEXT_FORMAT = "%s/version";

    public static final String SWAGGER2_CONTEXT_FORMAT = "%s/swagger/2";

    private String pathPrefix;

    private String luaContext;

    private String healthContext;

    private String versionContext;

    private String swaggerContext;

    private Injector injector;

    private DeploymentManager deploymentManager;

    @Override
    protected void doStart() {
        List.of(
            new App(getDeploymentManager(), this, getLuaContext()),
//            new App(getDeploymentManager(), this, getHealthContext()),
//            new App(getDeploymentManager(), this, getVersionContext()),
            new App(getDeploymentManager(), this, getSwaggerContext())
        ).forEach(getDeploymentManager()::addApp);
    }

    @Override
    public ContextHandler createContextHandler(final App app) throws Exception {

        final var originId = app.getOriginId();

        if (originId.equals(getHealthContext())) {
            return createHealthContext(app);
        } else if (originId.equals(getLuaContext())) {
            return createLuaContext(app);
        } else if (originId.equals(getVersionContext())) {
            return createVersionContext(app);
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

        final var servletContextHandler = new ServletContextHandler();

        final var resourceBase = pathDocs.getPath().toAbsolutePath().toString();
        defaultServletHolder.setInitParameter("resourceBase", resourceBase);

        servletContextHandler.setContextPath(getLuaContext());
        servletContextHandler
            .getMimeTypes()
            .addMimeMapping("lua", "application/text");

        return servletContextHandler;

    }

    private ContextHandler createSwaggerContext(final App app) {

        if (!getSwaggerContext().equals(app.getOriginId())) {
            throw new IllegalStateException("Expected: " + getSwaggerContext());
        }

        final var injector = getInjector().createChildInjector(
            new DocJerseyModule(),
            new RestAPISecurityModule(),
            new RestAPIServicesModule(),
            new RedissonServicesModule(ServletScopes.REQUEST)
        );

        final var guiceFilter = injector.getInstance(GuiceFilter.class);
        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(getSwaggerContext());
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));

        return servletContextHandler;
    }

    private ContextHandler createHealthContext(final App app) {

        if (!getHealthContext().equals(app.getOriginId())) {
            throw new IllegalStateException("Expected: " + getHealthContext());
        }

        final var injector = getInjector().createChildInjector(new ServletModule() {
            @Override
            protected void configureServlets() {
            serve("/").with(HealthServlet.class);
            }
        });

        final var guiceFilter = injector.getInstance(GuiceFilter.class);
        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(getHealthContext());
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));

        return servletContextHandler;

    }

    private ContextHandler createVersionContext(final App app) {

        if (!getVersionContext().equals(app.getOriginId())) {
            throw new IllegalStateException("Expected: " + getVersionContext());
        }

        final var injector = getInjector().createChildInjector(new ServletModule() {
            @Override
            protected void configureServlets() {
                serve("/").with(VersionServlet.class);
            }
        });

        final var guiceFilter = injector.getInstance(GuiceFilter.class);
        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(getVersionContext());
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));

        return servletContextHandler;

    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public String getLuaContext() {
        return luaContext;
    }

    public String getHealthContext() {
        return healthContext;
    }

    public String getVersionContext() {
        return versionContext;
    }

    public String getSwaggerContext() {
        return swaggerContext;
    }

    @Inject
    public void setPathPrefix(@Named(HTTP_PATH_PREFIX) final String pathPrefix) {

        this.pathPrefix = pathPrefix;

        this.luaContext = format(LUA_CONTEXT_FORMAT, getPathPrefix());
        this.healthContext = format(HEALTH_CONTEXT_FORMAT, getPathPrefix());
        this.swaggerContext = format(SWAGGER2_CONTEXT_FORMAT, getPathPrefix());
        this.versionContext = format(VERSION_CONTEXT_FORMAT, getVersionContext());

        this.luaContext = this.luaContext.startsWith("/")
            ? this.luaContext
            : "/" + this.luaContext;

        this.healthContext = this.healthContext.startsWith("/")
            ? this.healthContext
            : "/" + this.healthContext;

        this.versionContext = this.versionContext.startsWith("/")
            ? this.versionContext
            : "/" + this.versionContext;

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
