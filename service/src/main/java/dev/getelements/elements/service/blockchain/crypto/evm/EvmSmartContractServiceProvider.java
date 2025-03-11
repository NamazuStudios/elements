package dev.getelements.elements.service.blockchain.crypto.evm;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.blockchain.EvmSmartContractInvocationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

public class EvmSmartContractServiceProvider implements Provider<EvmSmartContractInvocationService> {

    private User user;

    private Provider<SuperUserEvmSmartContractInvocationService> superUserEvmSmartContractServiceProvider;

    @Override
    public EvmSmartContractInvocationService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserEvmSmartContractServiceProvider().get();
            default:
                return forbidden(EvmSmartContractInvocationService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserEvmSmartContractInvocationService> getSuperUserEvmSmartContractServiceProvider() {
        return superUserEvmSmartContractServiceProvider;
    }

    @Inject
    public void setSuperUserEvmSmartContractServiceProvider(final Provider<SuperUserEvmSmartContractInvocationService> superUserEvmSmartContractServiceProvider) {
        this.superUserEvmSmartContractServiceProvider = superUserEvmSmartContractServiceProvider;
    }

}
