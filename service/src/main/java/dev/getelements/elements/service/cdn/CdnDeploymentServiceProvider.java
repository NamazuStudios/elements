package dev.getelements.elements.service.cdn;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.cdn.CdnDeploymentService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;

public class CdnDeploymentServiceProvider implements Provider<CdnDeploymentService> {

    @Inject
    private User user;

    @Inject
    private Provider<AnonCdnDeploymentService> anonDeploymentServiceProvider;

    @Inject
    private Provider<SuperuserDeploymentService> superuserDeploymentServiceProvider;

    @Override
    public CdnDeploymentService get() {
        if(user.getLevel() == SUPERUSER) {
            return superuserDeploymentServiceProvider.get();
        }
        return anonDeploymentServiceProvider.get();
    }
}
