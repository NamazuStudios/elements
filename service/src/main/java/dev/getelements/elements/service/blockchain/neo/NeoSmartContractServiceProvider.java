package dev.getelements.elements.service.blockchain.neo;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.Services;

import javax.inject.Inject;
import javax.inject.Provider;

public class NeoSmartContractServiceProvider implements Provider<NeoSmartContractService> {

    private User user;

    private Provider<SuperUserNeoSmartContractService> superUserNeoSmartContractService;

    @Override
    public NeoSmartContractService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserNeoSmartContractService().get();
            default:
                return Services.forbidden(NeoSmartContractService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserNeoSmartContractService> getSuperUserNeoSmartContractService() {
        return superUserNeoSmartContractService;
    }

    @Inject
    public void setSuperUserNeoSmartContractServiceProvider(Provider<SuperUserNeoSmartContractService> superUserNeoSmartContractService) {
        this.superUserNeoSmartContractService = superUserNeoSmartContractService;
    }

}
