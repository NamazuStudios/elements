package com.namazustudios.socialengine.service.blockchain.invoke.flow;

import com.namazustudios.socialengine.service.FlowSmartContractInvocationService;
import com.namazustudios.socialengine.service.blockchain.invoke.ScopedInvoker;

public class FlowScopedInvoker implements ScopedInvoker<FlowInvocationScope>, FlowSmartContractInvocationService.Invoker {

    @Override
    public Object send() {
        return null;
    }

    @Override
    public Object call() {
        return null;
    }

    @Override
    public void initialize(FlowInvocationScope evmInvocationScope) {

    }

}
