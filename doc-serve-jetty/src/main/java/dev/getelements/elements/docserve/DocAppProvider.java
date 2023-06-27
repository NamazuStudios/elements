package dev.getelements.elements.docserve;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletScopes;
import dev.getelements.elements.docserve.guice.DocJerseyModule;
import dev.getelements.elements.docserve.guice.LuaStaticPathDocsModule;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.guice.StandardServletSecurityModule;
import dev.getelements.elements.guice.StandardServletServicesModule;
import dev.getelements.elements.service.guice.RedissonServicesModule;
import dev.getelements.elements.servlet.HttpContextRoot;
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
import javax.servlet.DispatcherType;
import java.util.HashMap;
import java.util.List;

import static dev.getelements.elements.docserve.DocGuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME;
import static java.util.EnumSet.allOf;
import static org.eclipse.jetty.util.Loader.getResource;

public class DocAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final Logger logger = LoggerFactory.getLogger(DocAppProvider.class);

    public static final String LUA_CONTEXT = "/doc/lua";

    public static final String REST_CONTEXT = "/doc/rest";

    public static final String SWAGGER_CONTEXT = "/doc/swagger";

    private Injector injector;

    private HttpContextRoot httpContextRoot;

    private DeploymentManager deploymentManager;

    @Override
    protected void doStart() {

        final var luaContextRoot = getHttpContextRoot().normalize(LUA_CONTEXT);
        final var restContextRoot = getHttpContextRoot().normalize(REST_CONTEXT);
        final var swaggerContextRoot = getHttpContextRoot().normalize(SWAGGER_CONTEXT);

        List.of(
            new App(getDeploymentManager(), this, luaContextRoot, createLuaContext(luaContextRoot)),
            new App(getDeploymentManager(), this, restContextRoot, createRestContext(restContextRoot)),
            new App(getDeploymentManager(), this, swaggerContextRoot, createSwaggerContext(swaggerContextRoot))
        ).forEach(getDeploymentManager()::addApp);
    }

    @Override
    public ContextHandler createContextHandler(final App app) {
        throw new InternalException("No context handler for app: " + app);
    }

    private ContextHandler createLuaContext(final String luaContextRoot) {

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
        servletContextHandler.setContextPath(luaContextRoot);
        servletContextHandler
            .getMimeTypes()
            .addMimeMapping("lua", "text/plain");

        return servletContextHandler;

    }

    private ContextHandler createRestContext(final String restContextRoot) {

        final var injector = getInjector().createChildInjector(
            new DocJerseyModule(),
            new StandardServletSecurityModule(),
            new StandardServletServicesModule(),
            new RedissonServicesModule(ServletScopes.REQUEST)
        );

        final var servletContextHandler = new ServletContextHandler();

        final var guiceFilter = injector.getInstance(GuiceFilter.class);
        servletContextHandler.getServletContext().setAttribute(INJECTOR_ATTRIBUTE_NAME, injector);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));
        servletContextHandler.setContextPath(restContextRoot);

        return servletContextHandler;

    }

    private ContextHandler createSwaggerContext(final String swaggerContextRoot) {

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(swaggerContextRoot);

        final var redirect = injector.createChildInjector().getInstance(DocRedirectFilter.class);
        servletContextHandler.addFilter(new FilterHolder(redirect), "/*", allOf(DispatcherType.class));

        final var defaultServletHolder = servletContextHandler.addServlet(DefaultServlet.class, "/*");

        final var defaultInitParameters = new HashMap<String, String>();
        defaultInitParameters.put("dirAllowed", "false");
        defaultInitParameters.put("resourceBase", getResource("swagger").toString());
        defaultServletHolder.setInitParameters(defaultInitParameters);

        return servletContextHandler;

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

    public HttpContextRoot getHttpContextRoot() {
        return httpContextRoot;
    }

    @Inject
    public void setHttpContextRoot(HttpContextRoot httpContextRoot) {
        this.httpContextRoot = httpContextRoot;
    }

}
