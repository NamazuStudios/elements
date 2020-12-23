package com.namazustudios.socialengine.cdnserve;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.cdnserve.guice.CdnGuiceResourceConfig;
import com.namazustudios.socialengine.cdnserve.guice.CdnJerseyModule;
import com.namazustudios.socialengine.cdnserve.guice.GitServletModule;
import com.namazustudios.socialengine.codeserve.GitSecurityModule;
import com.namazustudios.socialengine.codeserve.api.deploy.DeploymentResource;
import com.namazustudios.socialengine.rest.guice.GuiceResourceConfig;
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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.DispatcherType;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.EnumSet;

import static java.lang.String.format;
import static java.util.EnumSet.allOf;

public class CdnAppProvider extends AbstractLifeCycle implements AppProvider {

    public static final String GIT_CONTEXT = "git";

    public static final String MANAGE_CONTEXT = "manage";

    public static final String CDN_ORIGIN_CONTEXT = "cdn";

    private static final String baseApiContext = "/cdn";

    private static final String staticApiContext = baseApiContext + "/static";

    private String contentDirectory;

    private Injector injector;

    private DeploymentManager deploymentManager;

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

        final Injector injector = getInjector().createChildInjector(new GitServletModule());
        injector.injectMembers(new GitSecurityModule());
        final VersionServlet versionServlet = injector.getInstance(VersionServlet.class);
        final GitServlet gitServlet = injector.getInstance(GitServlet.class);
        final GuiceFilter guiceFilter = injector.getInstance(GuiceFilter.class);
        final HttpServletBasicAuthFilter authFilter = injector.getInstance(HttpServletBasicAuthFilter.class);

        final ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(baseApiContext);
        servletContextHandler.addFilter(new FilterHolder(authFilter), "/*", allOf(DispatcherType.class));
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));
        servletContextHandler.addServlet(new ServletHolder(versionServlet), "/version");
        servletContextHandler.addServlet(new ServletHolder(gitServlet), format("/%s/*", GIT_CONTEXT));
        return servletContextHandler;
    }

    private ContextHandler createManageContext(final App app) {
        if (!MANAGE_CONTEXT.equals(app.getOriginId())) {
            throw new IllegalArgumentException("App must have origin ID: " + MANAGE_CONTEXT);
        }

        final Injector injector = getInjector().createChildInjector(new CdnJerseyModule("cdn") {
            @Override
            protected void configureResoures() {
                enableAllResources();
            }
        });
        final GuiceFilter guiceFilter = injector.getInstance(GuiceFilter.class);

        final ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath(baseApiContext);
        ServletHolder servletHandler = servletContextHandler.addServlet(ServletContainer.class, "/*");
        servletHandler.setInitParameter(
                "jersey.config.server.provider.classnames",
                DeploymentResource.class.getCanonicalName());
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));
        servletContextHandler.setAttribute(CdnGuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME, injector);
        return servletContextHandler;
    }

    private ContextHandler createCdnContext(final App app) throws IOException {
        if (!CDN_ORIGIN_CONTEXT.equals(app.getOriginId())) {
            throw new IllegalArgumentException("App must have origin ID: " + CDN_ORIGIN_CONTEXT);
        }
        ServletContextHandler ctx = new ServletContextHandler();
        ctx.setContextPath(staticApiContext);

        DefaultServlet defaultServlet = new DefaultServlet();
        ServletHolder holderPwd = new ServletHolder("default", defaultServlet);
        holderPwd.setInitParameter("resourceBase", getContentDirectory());

        ctx.addServlet(holderPwd, "/*");
        return ctx;
    }

    @Override
    protected void doStart() throws Exception {
        startGitApp();
        startManageApp();
        startCdnApp();
    }

    private void startGitApp() {
        final App app = new App(getDeploymentManager(), this, GIT_CONTEXT);
        getDeploymentManager().addApp(app);
    }

    private void startManageApp() {
        final App app = new App(getDeploymentManager(), this, MANAGE_CONTEXT);
        getDeploymentManager().addApp(app);
    }

    private void startCdnApp() {
        final App app = new App(getDeploymentManager(), this, CDN_ORIGIN_CONTEXT);
        getDeploymentManager().addApp(app);
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
    private String setContentDirectory(@Named(Constants.CDN_FILE_DIRECTORY)String contentDirectory) {
        return this.contentDirectory = contentDirectory;
    }
}
