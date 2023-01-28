package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.service.blockchain.invoke.ScopedInvoker;
import com.namazustudios.socialengine.service.blockchain.invoke.flow.FlowInvocationScope;

import java.util.List;
import java.util.regex.Pattern;

public interface FlowSmartContractInvocationService extends SmartContractInvocationService<FlowSmartContractInvocationService.Invoker> {

    /**
     * Validates a smart contract address.
     */
    Pattern CONTRACT_ADDRESS = Pattern.compile("0[xX][0-9a-fA-F]+");

    /**
     * Used to make invocations on the Flow blockchain.
     */
    interface Invoker extends ScopedInvoker<FlowInvocationScope> {

        Object send(String script, List<?> arguments);

        Object call();

    }

}
