package dev.getelements.elements.service.blockchain.omni;

import dev.getelements.elements.sdk.model.user.User;

import dev.getelements.elements.sdk.service.blockchain.SmartContractService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

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
