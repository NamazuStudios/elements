package dev.getelements.elements.service.application;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.PSNApplicationConfigurationService;
import dev.getelements.elements.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

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
