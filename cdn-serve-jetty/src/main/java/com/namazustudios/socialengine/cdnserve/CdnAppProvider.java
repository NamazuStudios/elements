package com.namazustudios.socialengine.cdnserve;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

public class CdnAppProvider extends AbstractLifeCycle implements AppProvider {

    public static final String GIT_CONTEXT = "git";

    public static final String MANAGE_CONTEXT = "manage";

    public static final String CDN_ORIGIN_CONTEXT = "cdn";

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
        throw new UnsupportedOperationException();
    }

    private ContextHandler createManageContext(final App app) {
        throw new UnsupportedOperationException();
    }

    private ContextHandler createCdnContext(final App app) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doStart() throws Exception {
        startGitApp();
        startManageApp();
    }

    private void startGitApp() {
        final App app = new App(getDeploymentManager(), this, GIT_CONTEXT);
        getDeploymentManager().addApp(app);
    }

    private void startManageApp() {
        final App app = new App(getDeploymentManager(), this, MANAGE_CONTEXT);
        getDeploymentManager().addApp(app);
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
    }

}
