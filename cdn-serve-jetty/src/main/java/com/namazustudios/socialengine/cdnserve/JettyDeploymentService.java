package com.namazustudios.socialengine.cdnserve;

import org.eclipse.jetty.deploy.DeploymentManager;

import javax.inject.Inject;

public class JettyDeploymentService implements DeploymentService {

    private DeploymentManager deploymentManager;

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Inject
    public void setDeploymentManager(DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

}
