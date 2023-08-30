package dev.getelements.elements.cdnserve;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import dev.getelements.elements.git.GitSecurityModule;
import dev.getelements.elements.git.GitServletModule;
import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.guice.StandardServletRedissonServicesModule;
import dev.getelements.elements.guice.StandardServletSecurityModule;
import dev.getelements.elements.guice.StandardServletServicesModule;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.rt.git.GitApplicationBootstrapperModule;
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

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.DispatcherType;

import static dev.getelements.elements.Constants.CDN_FILE_DIRECTORY;
import static dev.getelements.elements.Constants.CDN_SERVE_ENDPOINT;
import static java.lang.String.format;
import static java.util.EnumSet.allOf;

public class CdnAppProvider extends AbstractLifeCycle implements AppProvider {

    public static final String GIT_CONTEXT = "/cdn/git";

    public static final String MANAGE_CONTEXT = "/cdn/manage";

    public static final String OBJECTS_ORIGIN_CONTEXT = "/cdn/object";

    public static final String STATIC_ORIGIN_CONTEXT_FORMAT = "/cdn/static/app/%s";

    private String contentDirectory;

    private String serveEndpoint;

    private Injector injector;

    private DeploymentManager deploymentManager;

    private ApplicationDao applicationDao;

    private HttpContextRoot httpContextRoot;

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Override
    public void setDeploymentManager(final DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    @Override
    public ContextHandler createContextHandler(final App app) {
        throw new InternalException("No Context Handler.");
    }

    @Override
    protected void doStart() {
        startGitApp();
        startManageApp();
        startObjectsApp();
        startStaticApps();
    }

    private void startGitApp() {

        final var gitContext = getHttpContextRoot().normalize(GIT_CONTEXT);

        final var injector = getInjector().createChildInjector(
                new GitServletModule(),
                new GitSecurityModule(),
                new CdnServeStorageModule(),
                new StandardServletServicesModule(),
                new StandardServletRedissonServicesModule(),
                new GitApplicationBootstrapperModule().withBareRepository()
        );

        final var guiceFilter = injector.getInstance(GuiceFilter.class);

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(gitContext);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));

        final App app = new App(getDeploymentManager(), this, gitContext, servletContextHandler);
        getDeploymentManager().addApp(app);

    }

    private void startManageApp() {

        final var injector = getInjector().createChildInjector(
                new CdnJerseyModule(),
                new CdnServicesModule(),
                new CdnServeStorageModule(),
                new StandardServletSecurityModule(),
                new StandardServletServicesModule(),
                new StandardServletRedissonServicesModule(),
                new GitApplicationBootstrapperModule().withBareRepository()
        );

        final var manageContext = getHttpContextRoot().normalize(MANAGE_CONTEXT);
        final var guiceFilter = injector.getInstance(GuiceFilter.class);

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(manageContext);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));
        servletContextHandler.setAttribute(CdnGuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME, injector);

        final App app = new App(getDeploymentManager(), this, manageContext, servletContextHandler);
        getDeploymentManager().addApp(app);

    }

    private void startObjectsApp() {

        final var injector = getInjector().createChildInjector(
                new CdnObjectOriginModule(),
                new StandardServletSecurityModule(),
                new StandardServletServicesModule(),
                new StandardServletRedissonServicesModule()
        );

        final var objectsOriginContext = getHttpContextRoot().normalize(OBJECTS_ORIGIN_CONTEXT);
        final var guiceFilter = injector.getInstance(GuiceFilter.class);

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(objectsOriginContext);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));

        final App app = new App(getDeploymentManager(), this, objectsOriginContext, servletContextHandler);
        getDeploymentManager().addApp(app);

    }

    private void startStaticApps() {

        final var applications = getApplicationDao().getActiveApplications();

        for(final var  application : applications) {

            final App app = new App(
                    getDeploymentManager(),
                    this,
                    application.getName(),
                    createCdnContext(application)
            );

            getDeploymentManager().addApp(app);
        }

    }

    private ContextHandler createCdnContext(final Application application) {

        final var ctx = new ServletContextHandler();
        final var path = getHttpContextRoot().formatNormalized(STATIC_ORIGIN_CONTEXT_FORMAT, application.getName());
        ctx.setContextPath(path);

        final var defaultServletHolder = new ServletHolder("default", new DefaultServlet());

        final var resourceBase = format("%s/%s/%s", getContentDirectory(), application.getId(), getServeEndpoint());
        defaultServletHolder.setInitParameter("resourceBase", resourceBase);
        ctx.addServlet(defaultServletHolder, "/*");

        return ctx;

    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    private String getContentDirectory() {
        return contentDirectory;
    }

    @Inject
    private void setContentDirectory(@Named(CDN_FILE_DIRECTORY) String contentDirectory) {
        this.contentDirectory = contentDirectory;
    }

    private String getServeEndpoint() {
        return serveEndpoint;
    }

    @Inject
    private void setServeEndpoint(@Named(CDN_SERVE_ENDPOINT) String serveEndpoint) {
        this.serveEndpoint = serveEndpoint;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public HttpContextRoot getHttpContextRoot() {
        return httpContextRoot;
    }

    @Inject
    public void setHttpContextRoot(HttpContextRoot httpContextRoot) {
        this.httpContextRoot = httpContextRoot;
    }

}
