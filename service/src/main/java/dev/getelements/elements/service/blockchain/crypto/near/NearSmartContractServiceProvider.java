package dev.getelements.elements.service.blockchain.crypto.near;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.blockchain.NearSmartContractInvocationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

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
