package com.namazustudios.socialengine.service.blockchain.invoke.flow;

import com.namazustudios.socialengine.service.FlowSmartContractInvocationService;
import com.namazustudios.socialengine.service.SmartContractInvocationResolution;
import org.onflow.sdk.FlowAccessApi;
import org.onflow.sdk.FlowTransaction;

import javax.inject.Inject;

public class SuperUserFlowSmartContractInvocationService implements FlowSmartContractInvocationService {

    private Pattern

    private FlowAccessApi flowAccessApi;

    @Override
    public SmartContractInvocationResolution<Invoker> resolve() {

        final var script = "import TheContract from 0xABCD\nTheContract.foo()";

        final FlowTransaction flowTransaction = new FlowTransaction(
        );


        return null;
    }

    public FlowAccessApi getFlowAccessApi() {
        return flowAccessApi;
    }

    @Inject
    public void setFlowAccessApi(FlowAccessApi flowAccessApi) {
        this.flowAccessApi = flowAccessApi;
    }

}
