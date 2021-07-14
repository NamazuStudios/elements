package com.namazustudios.socialengine.docserve;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

public class DocAppProvider extends AbstractLifeCycle implements AppProvider {

    @Override
    public void setDeploymentManager(final DeploymentManager deploymentManager) {

    }

    @Override
    public ContextHandler createContextHandler(final App app) throws Exception {
        return null;
    }

}
