package dev.getelements.elements.service.blockchain.invoke.near;

import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.NearSmartContractInvocationService;

import javax.inject.Inject;
import javax.inject.Provider;

import static dev.getelements.elements.service.Services.forbidden;

public class NearSmartContractServiceProvider implements Provider<NearSmartContractInvocationService> {


    private User user;

    private Provider<SuperUserNearSmartContractInvocationService> superUserNearSmartContractInvocationServiceProvider;

    @Override
    public NearSmartContractInvocationService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserNearSmartContractInvocationServiceProvider().get();
            default:
                return forbidden(NearSmartContractInvocationService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(final User user) {
        this.user = user;
    }

    public Provider<SuperUserNearSmartContractInvocationService> getSuperUserNearSmartContractInvocationServiceProvider() {
        return superUserNearSmartContractInvocationServiceProvider;
    }

    @Inject
    public void setSuperUserNearSmartContractInvocationServiceProvider(final Provider<SuperUserNearSmartContractInvocationService> superUserNearSmartContractInvocationServiceProvider) {
        this.superUserNearSmartContractInvocationServiceProvider = superUserNearSmartContractInvocationServiceProvider;
    }
}
