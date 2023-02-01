package com.namazustudios.socialengine.service.blockchain.invoke.flow;

import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.FlowSmartContractInvocationService;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.service.Services.forbidden;

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
