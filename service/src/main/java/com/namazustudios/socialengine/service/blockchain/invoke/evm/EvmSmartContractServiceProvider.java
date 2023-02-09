package com.namazustudios.socialengine.service.blockchain.invoke.evm;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.EvmSmartContractInvocationService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

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
