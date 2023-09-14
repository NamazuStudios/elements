package dev.getelements.elements.appserve;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.servlet.GuiceFilter;
import dev.getelements.elements.appserve.guice.AppServeDispatcherModule;
import dev.getelements.elements.appserve.guice.AppServeDispatcherServletModule;
import dev.getelements.elements.appserve.guice.RemoteInvocationDispatcherModule;
import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.guice.StandardServletRedissonServicesModule;
import dev.getelements.elements.guice.StandardServletSecurityModule;
import dev.getelements.elements.guice.StandardServletServicesModule;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.guice.FilterModule;
import dev.getelements.elements.rt.guice.GuiceIoCResolverModule;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQContextModule;
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
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Context.REMOTE;

public class AppServeDispatcherAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final Logger logger = LoggerFactory.getLogger(AppServeDispatcherAppProvider.class);

    private static final String APP_PREFIX_FORMAT = "/app/%s/rest";

    private final ConcurrentMap<String, Injector> applicationInjectorMap = new ConcurrentHashMap<>();

    private Injector injector;

    private ApplicationDao applicationDao;

    private DeploymentManager deploymentManager;

    private HttpContextRoot httpContextRoot;

    @Override
    public ContextHandler createContextHandler(final App app) {
        throw new InternalException("Call not expected.");
    }


    @Override
    protected void doStart() {
        getApplicationDao()
                .getActiveApplications()
                .getObjects()
                .forEach(this::deploy);
    }

    private void deploy(final Application application) {
        try {

            final var app = new App(
                    getDeploymentManager(),
                    this,
                    application.getId(),
                    createContextHandlerForApplication(application)
            );

            getDeploymentManager().addApp(app);

        } catch (Exception ex) {
            logger.error("Failed to deploy application {} ", application.getName(), ex);
        }
    }


    public ContextHandler createContextHandlerForApplication(final Application application) {

        final var injector = injectorFor(application);

        final var path = getHttpContextRoot().formatNormalized(APP_PREFIX_FORMAT, application.getName());
        final var guiceFilter = injector.getInstance(GuiceFilter.class);

        logger.info("Running application {} at path {}.", application.getName(), path);

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(path);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", EnumSet.allOf(DispatcherType.class));

        return servletContextHandler;

    }

    private Injector injectorFor(final Application application) {
        return applicationInjectorMap.computeIfAbsent(application.getId(), k -> {

            final var injector = getInjector().createChildInjector(
                    new FilterModule(),
                    new GuiceIoCResolverModule(),
                    new AppServeDispatcherModule(),
                    new AppServeDispatcherServletModule(),
                    new StandardServletServicesModule(),
                    new StandardServletSecurityModule(),
                    new StandardServletRedissonServicesModule(),
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

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Override
    public void setDeploymentManager(DeploymentManager deploymentManager) {
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
