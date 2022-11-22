package com.namazustudios.socialengine.formidium;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.namazustudios.socialengine.guice.StandardServletRedissonServicesModule;
import com.namazustudios.socialengine.guice.StandardServletSecurityModule;
import com.namazustudios.socialengine.guice.StandardServletServicesModule;
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

import static com.namazustudios.socialengine.Constants.HTTP_PATH_PREFIX;
import static com.namazustudios.socialengine.service.formidium.FormidiumConstants.*;
import static com.namazustudios.socialengine.servlet.security.HttpPathUtils.normalize;
import static java.lang.String.format;

public class FormidiumAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final Logger logger = LoggerFactory.getLogger(FormidiumProxyServlet.class);

    private Injector injector;

    private String rootContext;

    private String formidiumApiKey;

    private String formidiumApiUrl;

    private String formidiumContext;

    private DeploymentManager deploymentManager;

    @Override
    protected void doStart() throws Exception {

        final var formidiumContextRoot = normalize(format("%s/%s", getRootContext(), getFormidiumContext()));

        if (getFormidiumApiKey().isBlank()) {
            logger.info("No Formidium API Key Configured. Disabling Formidium support.");
        } else {

            logger.info("Formidium API Key Configured. Enabling Formidium support.");

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

    public String getRootContext() {
        return rootContext;
    }

    @Inject
    public void setRootContext(@Named(HTTP_PATH_PREFIX) String rootContext) {
        this.rootContext = rootContext;
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

    public String getFormidiumContext() {
        return formidiumContext;
    }

    @Inject
    public void setFormidiumContext(@Named(FORMIDIUM_CONTEXT_ROOT) String formidiumContext) {
        this.formidiumContext = formidiumContext;
    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

}
