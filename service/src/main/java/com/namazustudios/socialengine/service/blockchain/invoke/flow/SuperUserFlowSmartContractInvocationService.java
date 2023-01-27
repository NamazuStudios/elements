package com.namazustudios.socialengine.service.blockchain.invoke.flow;

import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.service.FlowSmartContractInvocationService;
import com.namazustudios.socialengine.service.SmartContractInvocationResolution;
import org.onflow.sdk.FlowAccessApi;
import org.onflow.sdk.FlowTransaction;

import javax.inject.Inject;
import java.util.regex.Pattern;

public class SuperUserFlowSmartContractInvocationService implements FlowSmartContractInvocationService {

    private static final Pattern METHOD_NAME =Pattern.compile("\\w+");

    private static final Pattern CONTRACT_ADDRESS = Pattern.compile("0[xX][0-9a-fA-F]+");

    private static final String SCRIPT_HEADER = "import %s";

    private FlowAccessApi flowAccessApi;

    @Override
    public SmartContractInvocationResolution<Invoker> resolve(
            final String contractNameOrId,
            final BlockchainNetwork network) {
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
