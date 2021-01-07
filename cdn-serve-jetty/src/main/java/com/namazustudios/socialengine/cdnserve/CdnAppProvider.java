package com.namazustudios.socialengine.cdnserve;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.cdnserve.guice.CdnJerseyModule;
import com.namazustudios.socialengine.cdnserve.guice.GitServletModule;
import com.namazustudios.socialengine.cdnserve.api.DeploymentService;
import com.namazustudios.socialengine.cdnserve.guice.CdnGuiceResourceConfig;
import com.namazustudios.socialengine.codeserve.GitSecurityModule;
import com.namazustudios.socialengine.model.Deployment;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.service.ApplicationService;
import com.namazustudios.socialengine.servlet.security.HttpServletBasicAuthFilter;
import com.namazustudios.socialengine.servlet.security.VersionServlet;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jgit.http.server.GitServlet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.DispatcherType;

import java.io.IOException;
import java.util.LinkedHashSet;

import static java.lang.String.format;
import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toCollection;

public class CdnAppProvider extends AbstractLifeCycle implements AppProvider {

    public static final String GIT_CONTEXT = "/cdn/git";

    public static final String MANAGE_CONTEXT = "/cdn/manage";

    public static final String STATIC_ORIGIN_CONTEXT = "/cdn/static";

    private String contentDirectory;

    private String serveEndpoint;

    private Injector injector;

    private DeploymentManager deploymentManager;

    private ApplicationService applicationService;

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Override
    public void setDeploymentManager(final DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    @Override
    public ContextHandler createContextHandler(final App app) throws Exception {
        switch (app.getOriginId()) {
            case GIT_CONTEXT:    return createGitContext(app);
            case MANAGE_CONTEXT: return createManageContext(app);
            default:             return createCdnContext(app);
        }
    }

    private ContextHandler createGitContext(final App app) {
        if (!GIT_CONTEXT.equals(app.getOriginId())) {
            throw new IllegalArgumentException("App must have origin ID: " + GIT_CONTEXT);
        }

        final Injector injector = getInjector().createChildInjector(new GitServletModule(), new GitSecurityModule());
        final VersionServlet versionServlet = injector.getInstance(VersionServlet.class);
        final GitServlet gitServlet = injector.getInstance(GitServlet.class);
        final GuiceFilter guiceFilter = injector.getInstance(GuiceFilter.class);
        final HttpServletBasicAuthFilter authFilter = injector.getInstance(HttpServletBasicAuthFilter.class);

        final ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(GIT_CONTEXT);
        servletContextHandler.addFilter(new FilterHolder(authFilter), "/*", allOf(DispatcherType.class));
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));
        servletContextHandler.addServlet(new ServletHolder(versionServlet), "/version");
        servletContextHandler.addServlet(new ServletHolder(gitServlet), "/*");
        return servletContextHandler;
    }

    private ContextHandler createManageContext(final App app) {

        if (!MANAGE_CONTEXT.equals(app.getOriginId())) {
            throw new IllegalArgumentException("App must have origin ID: " + MANAGE_CONTEXT);
        }

        final Injector injector = getInjector().createChildInjector(new CdnJerseyModule());
        final GuiceFilter guiceFilter = injector.getInstance(GuiceFilter.class);

        final ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(MANAGE_CONTEXT);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));
        servletContextHandler.setAttribute(CdnGuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME, injector);

        return servletContextHandler;

    }

    private ContextHandler createCdnContext(final App app) throws IOException {

        final ServletContextHandler ctx = new ServletContextHandler();
        ctx.setContextPath(format("%s/%s/%s", STATIC_ORIGIN_CONTEXT, app.getOriginId(), getServeEndpoint()));

        final DefaultServlet defaultServlet = new DefaultServlet();
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
        final App app = new App(getDeploymentManager(), this, GIT_CONTEXT);
        getDeploymentManager().addApp(app);
    }

    private void startManageApp() {
        final App app = new App(getDeploymentManager(), this, MANAGE_CONTEXT);
        getDeploymentManager().addApp(app);
    }

    private void startCdnApps() {
        Pagination<Application> applications = getApplicationService().getApplications();
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

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    @Inject
    public void setApplicationService(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }
}
