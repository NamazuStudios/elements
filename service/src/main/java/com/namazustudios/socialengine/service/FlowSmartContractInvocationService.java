package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;
import com.namazustudios.socialengine.service.blockchain.invoke.ScopedInvoker;
import com.namazustudios.socialengine.service.blockchain.invoke.flow.FlowInvocationScope;

import java.util.List;
import java.util.regex.Pattern;

/**
 * A service which allows for invocation of Flow based smart contracts.
 */
@Expose({
        @ModuleDefinition(
                value = "namazu.elements.service.smartcontract.flow"),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.smartcontract.flow",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )
})
public interface FlowSmartContractInvocationService extends SmartContractInvocationService<FlowSmartContractInvocationService.Invoker> {

    /**
     * Validates a smart contract address.
     */
    Pattern CONTRACT_ADDRESS = Pattern.compile("0[xX][0-9a-fA-F]+");

    /**
     * Used to make invocations on the Flow blockchain.
     */
    interface Invoker extends ScopedInvoker<FlowInvocationScope> {

        /**
         * Sends a transaction to the blockchain.
         *
         * @param script the Cadence script to execute
         * @param argumentTypes the argument types to pass to the script
         * @param arguments the arguments passed to the script itself
         * @return the return value
         */
        Object send(String script, List<String> argumentTypes, List<?> arguments);

        /**
         * Exectues a script, but does not write, to the blockchain.
         *
         * @param script the Cadence script to execute
         * @param arguments the arguments passed to the script itself
         * @return the return value
         */
        Object call(String script, List<?> arguments);

    }

}
