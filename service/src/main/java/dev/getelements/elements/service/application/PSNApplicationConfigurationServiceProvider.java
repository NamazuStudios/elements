package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.application.PSNApplicationConfigurationService;
import dev.getelements.elements.service.util.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

/**
 * Created by patricktwohig on 5/24/17.
 */
public class PSNApplicationConfigurationServiceProvider implements Provider<PSNApplicationConfigurationService> {

    @Inject
    private User user;

    @Inject
    private Provider<PSNApplicationConfigurationService> superUserPSNApplicationProfileServiceProvider;

    @Override
    public PSNApplicationConfigurationService get() {
        switch (user.getLevel()) {
            case SUPERUSER:
                return superUserPSNApplicationProfileServiceProvider.get();
            default:
                return Services.forbidden(PSNApplicationConfigurationService.class);

        }
    }

}
