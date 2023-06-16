package dev.getelements.elements.formidium;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import dev.getelements.elements.guice.StandardServletRedissonServicesModule;
import dev.getelements.elements.guice.StandardServletSecurityModule;
import dev.getelements.elements.guice.StandardServletServicesModule;
import dev.getelements.elements.servlet.security.HttpContextRoot;
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
import javax.inject.Named;
import javax.servlet.DispatcherType;

import java.util.EnumSet;

import static dev.getelements.elements.Constants.HTTP_PATH_PREFIX;
import static dev.getelements.elements.service.formidium.FormidiumConstants.*;
import static dev.getelements.elements.servlet.security.HttpPathUtils.normalize;
import static java.lang.String.format;

public class FormidiumAppProvider extends AbstractLifeCycle implements AppProvider {

    /**
     * Specifies the formidium context root.
     */
    public static final String FORMIDIUM_CONTEXT_ROOT = "/api/formidium";

    private static final Logger logger = LoggerFactory.getLogger(FormidiumProxyServlet.class);

    private Injector injector;

    private String formidiumApiKey;

    private String formidiumApiUrl;

    private DeploymentManager deploymentManager;

    private HttpContextRoot httpContextRoot;

    @Override
    protected void doStart() throws Exception {

        if (getFormidiumApiKey().isBlank()) {
            logger.info("No Formidium API Key Configured. Disabling Formidium support.");
        } else {

            final var formidiumContextRoot = getHttpContextRoot().normalize(FORMIDIUM_CONTEXT_ROOT);

            logger.info("Formidium API Key Configured. Enabling Formidium support at {}.", formidiumContextRoot);

            final var app = new App(
                    getDeploymentManager(),
                    this,
                    formidiumContextRoot,
                    buildFormidiumApiContext(formidiumContextRoot)
            );

            deploymentManager.addApp(app);

        }

    }

    private ContextHandler buildFormidiumApiContext(final String formidiumContextRoot) {

        final var injector = this.injector.createChildInjector(
                new StandardServletSecurityModule(),
                new StandardServletServicesModule(),
                new StandardServletRedissonServicesModule(),
                new FormidiumServletModule(getFormidiumApiUrl())
        );

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(formidiumContextRoot);

        final var guiceFilter = injector.getInstance(GuiceFilter.class);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", EnumSet.allOf(DispatcherType.class));

        return servletContextHandler;

    }

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Override
    public void setDeploymentManager(DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    @Override
    public ContextHandler createContextHandler(App app) throws Exception {
        return null;
    }

    public String getFormidiumApiKey() {
        return formidiumApiKey;
    }

    @Inject
    public void setFormidiumApiKey(@Named(FORMIDIUM_API_KEY) String formidiumApiKey) {
        this.formidiumApiKey = formidiumApiKey;
    }

    public String getFormidiumApiUrl() {
        return formidiumApiUrl;
    }

    @Inject
    public void setFormidiumApiUrl(@Named(FORMIDIUM_API_URL) String formidiumApiUrl) {
        this.formidiumApiUrl = formidiumApiUrl;
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
