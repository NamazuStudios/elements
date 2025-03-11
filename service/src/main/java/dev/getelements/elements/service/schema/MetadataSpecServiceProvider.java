package dev.getelements.elements.service.schema;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.schema.MetadataSpecService;
import dev.getelements.elements.service.util.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class MetadataSpecServiceProvider implements Provider<MetadataSpecService> {

    private User user;

    private Provider<SuperUserMetadataSpecService> superUserTokenTemplateService;

    @Override
    public MetadataSpecService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserMetadataSpecService().get();
            default:
                return Services.forbidden(MetadataSpecService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }


    public Provider<SuperUserMetadataSpecService> getSuperUserMetadataSpecService() {
        return superUserTokenTemplateService;
    }

    @Inject
    public void setSuperUserTokenTemplateService(Provider<SuperUserMetadataSpecService> superUserMetadataSpecService) {
        this.superUserTokenTemplateService = superUserMetadataSpecService;
    }
}
