package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class SmartContractServiceProvider implements Provider<SmartContractService> {

    private User user;

    private Provider<SuperUserSmartContractService> superUserSmartContractServiceProvider;

    @Override
    public SmartContractService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserSmartContractServiceProvider().get();
            default:
                return forbidden(SmartContractService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserSmartContractService> getSuperUserSmartContractServiceProvider() {
        return superUserSmartContractServiceProvider;
    }

    @Inject
    public void setSuperUserSmartContractServiceProvider(Provider<SuperUserSmartContractService> superUserSmartContractServiceProvider) {
        this.superUserSmartContractServiceProvider = superUserSmartContractServiceProvider;
    }

}
