package dev.getelements.elements.appserve;

import com.google.inject.Injector;
import com.google.inject.Key;

import dev.getelements.elements.appserve.guice.*;
import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.guice.GuiceIoCResolverModule;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQContextModule;
import dev.getelements.elements.rt.servlet.DispatcherServlet;
import dev.getelements.elements.servlet.HttpContextRoot;
import dev.getelements.elements.servlet.security.*;
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
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.Context.REMOTE;
import static java.lang.String.format;

public class DispatcherAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final Logger logger = LoggerFactory.getLogger(DispatcherAppProvider.class);

    private static final String APP_PREFIX_FORMAT = "/app/%s/rest";

    private final ConcurrentMap<String, Injector> applicationInjectorMap = new ConcurrentHashMap<>();

    private Instance instance;

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

        getInstance().start();

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
        final var dispatcherServlet = injector.getInstance(DispatcherServlet.class);

        logger.info("Running application {} at path {}.", application.getName(), path);

        // Filters
        final var corsFilter = injector.getInstance(HttpServletCORSFilter.class);
        final var sessionIdAuthenticationFilter = injector.getInstance(HttpServletSessionIdAuthenticationFilter.class);

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(path);
        servletContextHandler.addServlet(new ServletHolder(dispatcherServlet), "/*");
        servletContextHandler.addFilter(new FilterHolder(corsFilter), "/*", EnumSet.allOf(DispatcherType.class));
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

    public Instance getInstance() {
        return instance;
    }

    @Inject
    public void setInstance(final Instance instance) {
        this.instance = instance;
    }

    public HttpContextRoot getHttpContextRoot() {
        return httpContextRoot;
    }

    @Inject
    public void setHttpContextRoot(HttpContextRoot httpContextRoot) {
        this.httpContextRoot = httpContextRoot;
    }

}
