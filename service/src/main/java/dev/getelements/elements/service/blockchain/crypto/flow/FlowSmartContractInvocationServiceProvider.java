package dev.getelements.elements.service.blockchain.crypto.flow;

import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.blockchain.FlowSmartContractInvocationService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.service.util.Services.forbidden;

public class FlowSmartContractInvocationServiceProvider implements Provider<FlowSmartContractInvocationService> {

    private User user;

    private Provider<SuperUserFlowSmartContractInvocationService> superUserFlowSmartContractInvocationServiceProvider;

    @Override
    public FlowSmartContractInvocationService get() {
        switch (getUser().getLevel()) {
            case SUPERUSER:
                return getSuperUserFlowSmartContractInvocationServiceProvider().get();
            default:
                return forbidden(FlowSmartContractInvocationService.class);
        }
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Provider<SuperUserFlowSmartContractInvocationService> getSuperUserFlowSmartContractInvocationServiceProvider() {
        return superUserFlowSmartContractInvocationServiceProvider;
    }

    @Inject
    public void setSuperUserFlowSmartContractInvocationServiceProvider(Provider<SuperUserFlowSmartContractInvocationService> superUserFlowSmartContractInvocationServiceProvider) {
        this.superUserFlowSmartContractInvocationServiceProvider = superUserFlowSmartContractInvocationServiceProvider;
    }

}
