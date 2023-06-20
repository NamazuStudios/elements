package dev.getelements.elements.codeserve;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import dev.getelements.elements.servlet.HttpContextRoot;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.DispatcherType;

import static dev.getelements.elements.rt.git.Constants.GIT_SCRIPT_STORAGE_DIRECTORY;
import static java.util.EnumSet.allOf;

public class CodeServeAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final String CODE_SERVE_CONTEXT_ROOT = "/code";

    private static final Logger logger = LoggerFactory.getLogger(CodeServeAppProvider.class);

    private Injector injector;

    private DeploymentManager deploymentManager;

    private HttpContextRoot httpContextRoot;

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Override
    protected void doStart() {

        final var injector = getInjector().createChildInjector(
                new GitServletModule(),
                new GitSecurityModule(),
                new CodeServeStorageModule(),
                new LuaBootstrapResourcesModule()
        );

        final var path = getHttpContextRoot().normalize(CODE_SERVE_CONTEXT_ROOT);
        logger.info("Running code serve at {}", path);

        final var filter = injector.getInstance(GuiceFilter.class);
        final var filterHolder = new FilterHolder(filter);

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(path);
        servletContextHandler.addFilter(filterHolder, "/*", allOf(DispatcherType.class));

        final var app = new App(
                getDeploymentManager(),
                this,
                path,
                servletContextHandler
        );

        getDeploymentManager().addApp(app);

    }

    @Override
    public void setDeploymentManager(final DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    @Override
    public ContextHandler createContextHandler(App app) throws Exception {
        throw new IllegalStateException("No context handler.");
    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public HttpContextRoot getHttpContextRoot() {
        return httpContextRoot;
    }

    @Inject
    public void setHttpContextRoot(HttpContextRoot httpContextRoot) {
        this.httpContextRoot = httpContextRoot;
    }

}
