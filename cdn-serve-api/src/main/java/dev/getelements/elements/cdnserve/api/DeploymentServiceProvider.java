package dev.getelements.elements.cdnserve.api;

import dev.getelements.elements.model.user.User;

import javax.inject.Inject;
import javax.inject.Provider;

import static dev.getelements.elements.model.user.User.Level.SUPERUSER;

public class DeploymentServiceProvider implements Provider<DeploymentService> {

    @Inject
    private User user;

    @Inject
    private Provider<AnonDeploymentService> anonDeploymentServiceProvider;

    @Inject
    private Provider<SuperuserDeploymentService> superuserDeploymentServiceProvider;

    @Override
    public DeploymentService get() {
        if(user.getLevel() == SUPERUSER) {
            return superuserDeploymentServiceProvider.get();
        }
        return anonDeploymentServiceProvider.get();
    }
}
