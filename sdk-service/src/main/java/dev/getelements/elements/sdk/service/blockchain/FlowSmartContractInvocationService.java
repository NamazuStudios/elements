package dev.getelements.elements.sdk.service.blockchain;

import dev.getelements.elements.sdk.model.blockchain.contract.FlowInvokeContractResponse;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.service.blockchain.invoke.ScopedInvoker;
import dev.getelements.elements.sdk.service.blockchain.invoke.flow.FlowInvocationScope;

import java.util.Collections;
import java.util.List;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * A service which allows for invocation of Flow based smart contracts.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface FlowSmartContractInvocationService extends SmartContractInvocationService<FlowSmartContractInvocationService.Invoker> {

    /**
     * Used to make invocations on the Flow blockchain.
     */
    interface Invoker extends ScopedInvoker<FlowInvocationScope> {

        /**
         * Sends a transaction to the blockchain.
         *
         * @param script the Cadence script to execute
         * @return the return value
         */
        default FlowInvokeContractResponse send(final String script) {
            return send(script, Collections.emptyList(), Collections.emptyList());
        }

        /**
         * Sends a transaction to the blockchain.
         *
         * @param script the Cadence script to execute
         * @param argumentTypes the argument types to pass to the script
         * @param arguments the arguments passed to the script itself
         * @return the return value
         */
        FlowInvokeContractResponse send(String script, List<String> argumentTypes, List<?> arguments);

        /**
         * Calls a script on the blockchain.
         *
         * @param script the Cadence script to execute
         * @return the return value
         */
        default Object call(final String script) {
            return call(script, Collections.emptyList(), Collections.emptyList());
        }

        /**
         * Exectues a script, but does not write, to the blockchain.
         *
         * @param script the Cadence script to execute
         * @param arguments the arguments passed to the script itself
         * @return the return value
         */
        Object call(String script, List<String> argumentTypes, List<?> arguments);

    }

}
