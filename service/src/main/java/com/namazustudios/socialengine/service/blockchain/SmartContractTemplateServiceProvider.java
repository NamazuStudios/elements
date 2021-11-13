package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class SmartContractTemplateServiceProvider implements Provider<SmartContractTemplateService> {

    private User user;

    private Provider<SuperUserSmartContractTemplateService> superUserSmartContractTemplateService;

    @Override
    public SmartContractTemplateService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserSmartContractTemplateService().get();
            default:
                return Services.forbidden(SmartContractTemplateService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserSmartContractTemplateService> getSuperUserSmartContractTemplateService() {
        return superUserSmartContractTemplateService;
    }

    @Inject
    public void setSmartContractTemplateServiceProvider(Provider<SuperUserSmartContractTemplateService> superUserSmartContractTemplateService) {
        this.superUserSmartContractTemplateService = superUserSmartContractTemplateService;
    }

}
