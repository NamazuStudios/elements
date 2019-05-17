package com.namazustudios.socialengine.appserve;

import com.google.inject.Injector;
import com.namazustudios.socialengine.appserve.guice.DispatcherModule;
import com.namazustudios.socialengine.appserve.guice.VersionServletModule;
import com.namazustudios.socialengine.dao.rt.GitLoader;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.ConnectionMultiplexer;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQClientModule;
import com.namazustudios.socialengine.rt.servlet.DispatcherServlet;
import com.namazustudios.socialengine.service.ApplicationService;
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
import javax.servlet.DispatcherType;
import java.io.File;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;

public class DispatcherAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final String PATH_PREFIX = "app-serve";
    private static final String VERSION_PREFIX = "app-serve-version";
    private static final String VERSION_ORIGIN_ID = "31a020f2-1df1-4b1a-8bc1-a50d2cabd823";

    private static final Logger logger = LoggerFactory.getLogger(DispatcherAppProvider.class);

    private final ConcurrentMap<String, Injector> applicationInjectorMap = new ConcurrentHashMap<>();

    private Injector injector;

    private DeploymentManager deploymentManager;

    private ApplicationService applicationService;

    private GitLoader gitLoader;

    private ConnectionMultiplexer connectionMultiplexer;

    @Override
    public ContextHandler createContextHandler(final App app) throws Exception {
        switch (app.getOriginId()) {
            case VERSION_ORIGIN_ID: return createContextHandlerForVersion(app);
            default:                return createContextHandlerForApplication(app);
        }
    }

    public ContextHandler createContextHandlerForVersion(final App app) {

        if (!VERSION_ORIGIN_ID.equals(app.getOriginId())) {
            throw new IllegalArgumentException("App must have origin ID: " + VERSION_ORIGIN_ID);
        }

        final Injector injector = getInjector().createChildInjector(new VersionServletModule());

        final VersionServlet versionServlet = injector.getInstance(VersionServlet.class);

        final ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");
        servletContextHandler.addServlet(new ServletHolder(versionServlet), format("/%s/*", VERSION_PREFIX));
        return servletContextHandler;

    }

    public ContextHandler createContextHandlerForApplication(final App app) {

        final Application application = getApplicationService().getApplication(app.getOriginId());

        final Injector injector = injectorFor(application);
        final Context context = injector.getInstance(Context.class);
        context.start();

        final String path = format("/%s/%s", PATH_PREFIX, application.getName());
        final DispatcherServlet dispatcherServlet = injector.getInstance(DispatcherServlet.class);
        final SessionIdAuthenticationFilter sessionIdAuthenticationFilter = injector.getInstance(SessionIdAuthenticationFilter.class);

        final ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(path);
        servletContextHandler.addServlet(new ServletHolder(dispatcherServlet), "/*");
        servletContextHandler.addFilter(new FilterHolder(sessionIdAuthenticationFilter), "/*", EnumSet.allOf(DispatcherType.class));
        return servletContextHandler;

    }

    private Injector injectorFor(final Application application) {
        return applicationInjectorMap.computeIfAbsent(application.getId(), k -> {

            final UUID uuid = getConnectionMultiplexer().getDestinationUUIDForNodeId(application.getId());
            getConnectionMultiplexer().open(application.getId());

            final String connectAddress = getConnectionMultiplexer().getConnectAddress(uuid);

            final File codeDirectory = getGitLoader().getCodeDirectory(application);
            final DispatcherModule dispatcherModule = new DispatcherModule(codeDirectory);
            final JeroMQClientModule jeroMQClientModule = new JeroMQClientModule()
                .withDefaultExecutorServiceProvider()
                .withConnectAddress(connectAddress);

            return getInjector().createChildInjector(dispatcherModule, jeroMQClientModule);

        });
    }

    @Override
    protected void doStart() throws Exception {
        getConnectionMultiplexer().start();

        final App version = new App(getDeploymentManager(), this, VERSION_ORIGIN_ID);
        getDeploymentManager().addApp(version);

        getApplicationService().getApplications().getObjects().forEach(this::deploy);

    }

    private void deploy(final Application application) {
        try {
            final App app = new App(getDeploymentManager(), this, application.getId());
            getDeploymentManager().addApp(app);
        } catch (Exception ex) {
            logger.error("Failed to deploy applciation {} ", application.getName(), ex);
        }
    }

    @Override
    protected void doStop() throws Exception {
        applicationInjectorMap
            .values()
            .stream()
            .map(i -> i.getInstance(Context.class))
            .forEach(this::shutdown);
        getConnectionMultiplexer().stop();
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
    public void setApplicationService(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Override
    public void setDeploymentManager(DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public GitLoader getGitLoader() {
        return gitLoader;
    }

    @Inject
    public void setGitLoader(GitLoader gitLoader) {
        this.gitLoader = gitLoader;
    }

    public ConnectionMultiplexer getConnectionMultiplexer() {
        return connectionMultiplexer;
    }

    @Inject
    public void setConnectionMultiplexer(ConnectionMultiplexer connectionMultiplexer) {
        this.connectionMultiplexer = connectionMultiplexer;
    }

}
