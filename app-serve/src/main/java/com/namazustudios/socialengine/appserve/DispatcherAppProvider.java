package com.namazustudios.socialengine.appserve;

import com.google.inject.Injector;
import com.google.inject.Key;

import com.namazustudios.socialengine.appserve.guice.AppServeDispatcherModule;
import com.namazustudios.socialengine.appserve.guice.RemoteInvocationDispatcherModule;
import com.namazustudios.socialengine.appserve.guice.VersionServletModule;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.remote.Instance;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQContextModule;
import com.namazustudios.socialengine.rt.servlet.DispatcherServlet;
import com.namazustudios.socialengine.service.ApplicationService;
import com.namazustudios.socialengine.service.Unscoped;
import com.namazustudios.socialengine.servlet.security.SessionIdAuthenticationFilter;
import com.namazustudios.socialengine.servlet.security.VersionServlet;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.Constants.HTTP_PATH_PREFIX;
import static com.namazustudios.socialengine.rt.Context.REMOTE;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

public class DispatcherAppProvider extends AbstractLifeCycle implements AppProvider {

    public static final String VERSION_ENDPOINT = "com.namazustudios.socialengine.http.appserve.version.endpoint";

    private static final Logger logger = LoggerFactory.getLogger(DispatcherAppProvider.class);

    private final String versionOriginId = randomUUID().toString();

    private final ConcurrentMap<String, Injector> applicationInjectorMap = new ConcurrentHashMap<>();

    private Injector injector;

    private Instance instance;

    private DeploymentManager deploymentManager;

    private ApplicationService applicationService;

    private String versionEndpoint;

    private String applicationPathPrefix;

    @Override
    public ContextHandler createContextHandler(final App app) throws Exception {
        if (versionOriginId.equals(app.getOriginId())) {
            return createContextHandlerForVersion(app);
        } else {
            return createContextHandlerForApplication(app);
        }
    }

    public ContextHandler createContextHandlerForVersion(final App app) {

        if (!versionOriginId.equals(app.getOriginId())) {
            throw new IllegalArgumentException("App must have origin ID: " + versionOriginId);
        }

        final Injector injector = getInjector().createChildInjector(new VersionServletModule());

        final VersionServlet versionServlet = injector.getInstance(VersionServlet.class);

        final ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");
        servletContextHandler.addServlet(new ServletHolder(versionServlet), getVersionEndpoint());
        return servletContextHandler;

    }

    public ContextHandler createContextHandlerForApplication(final App app) {

        final var application = getApplicationService().getApplication(app.getOriginId());

        final var injector = injectorFor(application);

        final var path = format("%s/%s", getApplicationPathPrefix(), application.getName()).replace("(//+)", "/");
        final var dispatcherServlet = injector.getInstance(DispatcherServlet.class);
        final var sessionIdAuthenticationFilter = injector.getInstance(SessionIdAuthenticationFilter.class);

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(path);
        servletContextHandler.addServlet(new ServletHolder(dispatcherServlet), "/*");
        servletContextHandler.addFilter(new FilterHolder(sessionIdAuthenticationFilter), "/*", EnumSet.allOf(DispatcherType.class));

        return servletContextHandler;

    }

    private Injector injectorFor(final Application application) {
        return applicationInjectorMap.computeIfAbsent(application.getId(), k -> {

            final var injector = getInjector().createChildInjector(
                new GuiceIoCResolverModule(),
                new AppServeDispatcherModule(),
                new RemoteInvocationDispatcherModule(),
                new JeroMQContextModule().withApplicationUniqueName(application.getId())
            );

            final var key = Key.get(Context.class, named(REMOTE));
            final var context = injector.getInstance(key);
            context.start();

            return injector;

        });
    }

    @Override
    protected void doStart() {

        getInstance().start();

        final var version = new App(getDeploymentManager(), this, versionOriginId);

        getDeploymentManager().addApp(version);
        getApplicationService().getApplications().getObjects().forEach(this::deploy);

    }

    private void deploy(final Application application) {
        try {
            final var app = new App(getDeploymentManager(), this, application.getId());
            getDeploymentManager().addApp(app);
        } catch (Exception ex) {
            logger.error("Failed to deploy application {} ", application.getName(), ex);
        }
    }

    @Override
    protected void doStop() throws Exception {

        applicationInjectorMap
            .values()
            .stream()
            .map(i -> i.getInstance(Context.class))
            .forEach(this::shutdown);

        getInstance().close();

    }

    private void shutdown(final Context context) {
        try {
            context.shutdown();
        } catch (Exception ex) {
            logger.error("Failed to stop context.", ex);
        }
    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    @Inject
    public void setApplicationService(@Unscoped ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Override
    public void setDeploymentManager(DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public Instance getInstance() {
        return instance;
    }

    @Inject
    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public String getVersionEndpoint() {
        return versionEndpoint;
    }

    @Inject
    public void setVersionEndpoint(@Named(VERSION_ENDPOINT) String versionEndpoint) {
        this.versionEndpoint = versionEndpoint.startsWith("/")
            ? versionEndpoint
            : "/" + versionEndpoint;
    }

    public String getApplicationPathPrefix() {
        return applicationPathPrefix;
    }

    @Inject
    public void setApplicationPathPrefix(@Named(HTTP_PATH_PREFIX) String applicationPathPrefix) {
        this.applicationPathPrefix = applicationPathPrefix.startsWith("/")
            ? applicationPathPrefix
            : "/" + applicationPathPrefix;
    }


}
