package com.namazustudios.socialengine.service.blockchain.evm;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.EvmSmartContractService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

public class EvmSmartContractServiceProvider implements Provider<EvmSmartContractService> {

    private User user;

    private Provider<SuperUserEvmSmartContractService> superUserEvmSmartContractServiceProvider;

    @Override
    public EvmSmartContractService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserEvmSmartContractServiceProvider().get();
            default:
                return forbidden(EvmSmartContractService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserEvmSmartContractService> getSuperUserEvmSmartContractServiceProvider() {
        return superUserEvmSmartContractServiceProvider;
    }

    @Inject
    public void setSuperUserEvmSmartContractServiceProvider(final Provider<SuperUserEvmSmartContractService> superUserEvmSmartContractServiceProvider) {
        this.superUserEvmSmartContractServiceProvider = superUserEvmSmartContractServiceProvider;
    }

}
