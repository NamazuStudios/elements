package com.namazustudios.socialengine.docserve;

import com.google.inject.Injector;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

import javax.inject.Inject;
import javax.inject.Named;

import static com.namazustudios.socialengine.Constants.HTTP_PATH_PREFIX;
import static java.lang.String.format;

public class DocAppProvider extends AbstractLifeCycle implements AppProvider {

    public static final String LUA_CONTEXT_FORMAT = "%s/lua";

    public static final String HEALTH_CONTEXT_FORMAT = "%s/health";

    public static final String SWAGGER2_CONTEXT_FORMAT = "%s/swagger/2";

    private String pathPrefix;

    private String luaContext;

    private String healthContext;

    private String swaggerContext;

    private Injector injector;

    private DeploymentManager deploymentManager;

    @Override
    protected void doStart() {
        startLuaApp();
        startHealthApp();
        startSwaggerApp();
    }

    private void startLuaApp() {
        final var app = new App(getDeploymentManager(), this, getLuaContext());
        getDeploymentManager().addApp(app);
    }

    private void startHealthApp() {
        final var app = new App(getDeploymentManager(), this, getHealthContext());
        getDeploymentManager().addApp(app);
    }

    private void startSwaggerApp() {
        final var app = new App(getDeploymentManager(), this, getSwaggerContext());
        getDeploymentManager().addApp(app);
    }

    @Override
    public ContextHandler createContextHandler(final App app) throws Exception {
        return null;
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

    public String getSwaggerContext() {
        return swaggerContext;
    }

    @Inject
    public void setPathPrefix(@Named(HTTP_PATH_PREFIX) String pathPrefix) {

        this.pathPrefix = pathPrefix;

        this.luaContext = format(LUA_CONTEXT_FORMAT, getPathPrefix());
        this.healthContext = format(HEALTH_CONTEXT_FORMAT, getPathPrefix());
        this.swaggerContext = format(SWAGGER2_CONTEXT_FORMAT, getPathPrefix());

        this.luaContext = this.luaContext.startsWith("/")
            ? this.luaContext
            : "/" + this.luaContext;

        this.healthContext = this.healthContext.startsWith("/")
            ? this.healthContext
            : "/" + this.healthContext;

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
