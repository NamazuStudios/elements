package dev.getelements.elements.cdnserve;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import dev.getelements.elements.Constants;
import dev.getelements.elements.cdnserve.guice.CdnGitServletModule;
import dev.getelements.elements.cdnserve.guice.CdnGuiceResourceConfig;
import dev.getelements.elements.cdnserve.guice.CdnJerseyModule;
import dev.getelements.elements.cdnserve.guice.CdnServeSecurityModule;
import dev.getelements.elements.codeserve.GitSecurityModule;
import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.servlet.security.HttpContextRoot;
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

    public static final String STATIC_ORIGIN_CONTEXT_FORMAT = "/cdn/content/%s";

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
        startCdnApps();
    }

    private void startGitApp() {

        final var gitContext = getHttpContextRoot().normalize(GIT_CONTEXT);

        final var injector = getInjector().createChildInjector(new CdnGitServletModule(), new GitSecurityModule());
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
                new CdnServeSecurityModule()
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

    private void startCdnApps() {

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
