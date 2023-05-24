package dev.getelements.elements.codeserve;

import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeServeAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final Logger logger = LoggerFactory.getLogger(CodeServeAppProvider.class);

    private DeploymentManager deploymentManager;

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Override
    protected void doStart() throws Exception {
        logger.info("TODO: Start Code Serve.");
    }

    @Override
    public void setDeploymentManager(final DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    @Override
    public ContextHandler createContextHandler(App app) throws Exception {
        throw new IllegalStateException("No context handler.");
    }

}
