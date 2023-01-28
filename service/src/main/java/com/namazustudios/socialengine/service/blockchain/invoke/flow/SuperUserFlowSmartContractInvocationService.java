package com.namazustudios.socialengine.service.blockchain.invoke.flow;

import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.service.FlowSmartContractInvocationService;
import com.namazustudios.socialengine.service.SmartContractInvocationResolution;
import com.namazustudios.socialengine.service.blockchain.invoke.ScopedInvoker;
import com.namazustudios.socialengine.service.blockchain.invoke.StandardSmartContractInvocationResolution;

import javax.inject.Inject;
import javax.inject.Provider;

public class SuperUserFlowSmartContractInvocationService implements FlowSmartContractInvocationService {

    private ScopedInvoker.Factory<FlowInvocationScope, Invoker> scopedInvokerFactory;

    private Provider<FlowInvoker> flowScopedInvokerProvider;

    private Provider<StandardSmartContractInvocationResolution<FlowInvocationScope, Invoker>> resolutionProvider;

    @Override
    public SmartContractInvocationResolution<Invoker> resolve(
            final String contractNameOrId,
            final BlockchainNetwork network) {
        final var resolution = getResolutionProvider().get();
        resolution.setScopedInvokerFactory(getScopedInvokerFactory());
        return resolution;
    }

    public ScopedInvoker.Factory<FlowInvocationScope, Invoker> getScopedInvokerFactory() {
        return scopedInvokerFactory;
    }

    @Inject
    public void setScopedInvokerFactory(ScopedInvoker.Factory<FlowInvocationScope, Invoker> scopedInvokerFactory) {
        this.scopedInvokerFactory = scopedInvokerFactory;
    }

    public Provider<FlowInvoker> getFlowScopedInvokerProvider() {
        return flowScopedInvokerProvider;
    }

    @Inject
    public void setFlowScopedInvokerProvider(Provider<FlowInvoker> flowScopedInvokerProvider) {
        this.flowScopedInvokerProvider = flowScopedInvokerProvider;
    }

    public Provider<StandardSmartContractInvocationResolution<FlowInvocationScope, Invoker>> getResolutionProvider() {
        return resolutionProvider;
    }

    @Inject
    public void setResolutionProvider(Provider<StandardSmartContractInvocationResolution<FlowInvocationScope, Invoker>> resolutionProvider) {
        this.resolutionProvider = resolutionProvider;
    }

}
