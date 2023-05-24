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
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.service.ApplicationService;
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
import java.io.IOException;

import static dev.getelements.elements.Constants.HTTP_PATH_PREFIX;
import static java.lang.String.format;
import static java.util.EnumSet.allOf;

public class CdnAppProvider extends AbstractLifeCycle implements AppProvider {

    public static final String GIT_CONTEXT_FORMAT = "%s/git";

    public static final String MANAGE_CONTEXT_FORMAT = "%s/manage";

    public static final String STATIC_ORIGIN_CONTEXT_FORMAT = "%s/static";

    private String pathPrefix;

    private String gitContext;

    private String manageContext;

    private String staticOriginContext;

    private String contentDirectory;

    private String serveEndpoint;

    private Injector injector;

    private DeploymentManager deploymentManager;

    private ApplicationDao applicationDao;

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Override
    public void setDeploymentManager(final DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    @Override
    public ContextHandler createContextHandler(final App app) throws Exception {

        final var originId = app.getOriginId();

        if (originId.equals(getGitContext())) {
            return createGitContext(app);
        } else if (originId.equals(getManageContext())) {
            return createManageContext(app);
        } else {
            return createCdnContext(app);
        }

    }

    private ContextHandler createGitContext(final App app) {

        if (!getGitContext().equals(app.getOriginId())) {
            throw new IllegalArgumentException("App must have origin ID: " + getGitContext());
        }

        final var injector = getInjector().createChildInjector(new CdnGitServletModule(), new GitSecurityModule());
        final var guiceFilter = injector.getInstance(GuiceFilter.class);

        final ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(getGitContext());
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));

        return servletContextHandler;

    }

    private ContextHandler createManageContext(final App app) {

        if (!getManageContext().equals(app.getOriginId())) {
            throw new IllegalArgumentException("App must have origin ID: " + getManageContext());
        }

        final var injector = getInjector().createChildInjector(
            new CdnJerseyModule(),
            new CdnServeSecurityModule()
        );

        final var guiceFilter = injector.getInstance(GuiceFilter.class);

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(getManageContext());
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));
        servletContextHandler.setAttribute(CdnGuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME, injector);

        return servletContextHandler;

    }

    private ContextHandler createCdnContext(final App app) throws IOException {

        final var ctx = new ServletContextHandler();
        ctx.setContextPath(format("%s/%s/%s", getStaticOriginContext(), app.getOriginId(), getServeEndpoint()));

        final var defaultServlet = new DefaultServlet();
        ServletHolder holderPwd = new ServletHolder("default", defaultServlet);
        holderPwd.setInitParameter("resourceBase", format("%s/%s/%s", getContentDirectory(), app.getOriginId(), getServeEndpoint()));
        ctx.addServlet(holderPwd, "/*");

        return ctx;

    }

    @Override
    protected void doStart() throws Exception {
        startGitApp();
        startManageApp();
        startCdnApps();
    }

    private void startGitApp() {
        final App app = new App(getDeploymentManager(), this, getGitContext());
        getDeploymentManager().addApp(app);
    }

    private void startManageApp() {
        final App app = new App(getDeploymentManager(), this, getManageContext());
        getDeploymentManager().addApp(app);
    }

    private void startCdnApps() {

        final var applications = getApplicationDao().getActiveApplications();

        for(Application a : applications){
            final App app = new App(getDeploymentManager(), this, a.getName());
            getDeploymentManager().addApp(app);
        }

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
    private void setContentDirectory(@Named(Constants.CDN_FILE_DIRECTORY)String contentDirectory) {
        this.contentDirectory = contentDirectory;
    }

    private String getServeEndpoint() {
        return serveEndpoint;
    }

    @Inject
    private void setServeEndpoint(@Named(Constants.CDN_SERVE_ENDPOINT)String serveEndpoint) {
        this.serveEndpoint = serveEndpoint;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    @Inject
    public void setPathPrefix(@Named(HTTP_PATH_PREFIX) String pathPrefix) {

        this.pathPrefix = pathPrefix;

        this.gitContext = format(GIT_CONTEXT_FORMAT, getPathPrefix());
        this.manageContext = format(MANAGE_CONTEXT_FORMAT, getPathPrefix());
        this.staticOriginContext =format(STATIC_ORIGIN_CONTEXT_FORMAT, getPathPrefix());

        this.gitContext = this.gitContext.startsWith("/")
            ? this.gitContext
            : "/" + this.gitContext;

        this.manageContext = this.manageContext.startsWith("/")
            ? this.manageContext
            : "/" + this.manageContext;

        this.staticOriginContext = this.staticOriginContext.startsWith("/")
            ? this.staticOriginContext
            : "/" + this.staticOriginContext;

    }

    public String getGitContext() {
        return gitContext;
    }

    public String getManageContext() {
        return manageContext;
    }

    public String getStaticOriginContext() {
        return staticOriginContext;
    }

}
