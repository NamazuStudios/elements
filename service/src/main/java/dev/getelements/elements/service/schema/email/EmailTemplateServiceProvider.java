package dev.getelements.elements.service.schema.email;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.schema.email.EmailTemplateService;
import dev.getelements.elements.sdk.service.Services;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class EmailTemplateServiceProvider implements Provider<EmailTemplateService> {

    private User user;

    private Provider<SuperUserEmailTemplateService> superUserEmailTemplateService;

    @Override
    public EmailTemplateService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserEmailTemplateService().get();
            default:
                return Services.forbidden(EmailTemplateService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserEmailTemplateService> getSuperUserEmailTemplateService() {
        return superUserEmailTemplateService;
    }

    @Inject
    public void setSuperUserEmailTemplateService(Provider<SuperUserEmailTemplateService> superUserEmailTemplateService) {
        this.superUserEmailTemplateService = superUserEmailTemplateService;
    }
}
