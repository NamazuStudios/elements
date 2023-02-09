package com.namazustudios.socialengine.service.blockchain.invoke.flow;

import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.service.FlowSmartContractInvocationService;
import com.namazustudios.socialengine.service.SmartContractInvocationResolution;
import com.namazustudios.socialengine.service.blockchain.invoke.ScopedInvoker;
import com.namazustudios.socialengine.service.blockchain.invoke.StandardSmartContractInvocationResolution;
import com.namazustudios.socialengine.service.blockchain.invoke.SuperUserSmartContractInvocationService;

import javax.inject.Inject;
import javax.inject.Provider;

public class SuperUserFlowSmartContractInvocationService
        extends SuperUserSmartContractInvocationService<FlowInvocationScope, FlowSmartContractInvocationService.Invoker>
        implements FlowSmartContractInvocationService {

    private static final long DEFAULT_GAS_LIMIT = 100L;

    private ScopedInvoker.Factory<FlowInvocationScope, Invoker> scopedInvokerFactory;

    private Provider<StandardSmartContractInvocationResolution<FlowInvocationScope, Invoker>> resolutionProvider;

    @Override
    protected FlowInvocationScope newInvocationScope() {
        final var scope = new FlowInvocationScope();
        scope.setGasLimit(DEFAULT_GAS_LIMIT);
        return scope;
    }

    @Override
    protected SmartContractInvocationResolution<Invoker> newResolution(final FlowInvocationScope flowInvocationScope) {
        final var resolution = getResolutionProvider().get();
        resolution.setScope(flowInvocationScope);
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

    public Provider<StandardSmartContractInvocationResolution<FlowInvocationScope, Invoker>> getResolutionProvider() {
        return resolutionProvider;
    }

    @Inject
    public void setResolutionProvider(Provider<StandardSmartContractInvocationResolution<FlowInvocationScope, Invoker>> resolutionProvider) {
        this.resolutionProvider = resolutionProvider;
    }

}
