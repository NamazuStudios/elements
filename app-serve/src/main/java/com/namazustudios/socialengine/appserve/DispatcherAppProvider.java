package com.namazustudios.socialengine.appserve;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.namazustudios.socialengine.appserve.guice.DispatcherModule;
import com.namazustudios.socialengine.appserve.guice.DispatcherServletLoader;
import com.namazustudios.socialengine.dao.rt.GitLoader;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.ConnectionMultiplexer;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.jeromq.Routing;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.ClusterClientContextModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQRemoteInvokerModule;
import com.namazustudios.socialengine.service.ApplicationService;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.EnumSet.allOf;

public class DispatcherAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherAppProvider.class);

    private final ConcurrentMap<String, Injector> applicationInjectorMap = new ConcurrentHashMap<>();

    private Injector injector;

    private DeploymentManager deploymentManager;

    private ApplicationService applicationService;

    private GitLoader gitLoader;

    private ConnectionMultiplexer connectionMultiplexer;

    @Override
    public ContextHandler createContextHandler(App app) throws Exception {

        final Application application = getApplicationService().getApplication(app.getOriginId());

        final Injector injector = injectFor(application);
        final Context context = injector.getInstance(Context.class);
        context.start();

        final ServletContextHandler servletContextHandler = new ServletContextHandler();

        servletContextHandler.addEventListener(new DispatcherServletLoader(injector));
        servletContextHandler.setContextPath("/" + application.getName());
        servletContextHandler.addFilter(GuiceFilter.class, "/*", allOf(DispatcherType.class));

        return servletContextHandler;

    }

    private Injector injectFor(final Application application) {
        return applicationInjectorMap.computeIfAbsent(application.getId(), k -> {

            final UUID uuid = getConnectionMultiplexer().getDestinationUUIDForNodeId(application.getId());
            getConnectionMultiplexer().open(application.getId());

            final String connectAddress = getConnectionMultiplexer().getConnectAddress(uuid);

            final File codeDirectory = getGitLoader().getCodeDirectory(application);
            final DispatcherModule dispatcherModule = new DispatcherModule(connectAddress, codeDirectory);
            final ClusterClientContextModule clusterClientContextModule = new ClusterClientContextModule();
            final JeroMQRemoteInvokerModule jeroMQRemoteInvokerModule = new JeroMQRemoteInvokerModule().withConnectAddress(connectAddress);
            return getInjector().createChildInjector(dispatcherModule, clusterClientContextModule, jeroMQRemoteInvokerModule);

        });
    }

    @Override
    protected void doStart() throws Exception {
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
