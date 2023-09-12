package dev.getelements.elements.service;

import dev.getelements.elements.model.blockchain.contract.near.NearInvokeContractResponse;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import dev.getelements.elements.service.blockchain.invoke.ScopedInvoker;
import dev.getelements.elements.service.blockchain.invoke.near.NearInvocationScope;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A service which allows for invocation of Near based smart contracts.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.smartcontract.near"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.unscoped.smartcontract.near",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.smartcontract.near",
                deprecated = @DeprecationDefinition("Use eci.elements.service.smartcontract.near instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.smartcontract.near",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use namazu.elements.service.unscoped.smartcontract.near instead.")
        )
})
public interface NearSmartContractInvocationService extends SmartContractInvocationService<NearSmartContractInvocationService.Invoker> {

    /**
     * Used to make invocations on the Near blockchain.
     */
    interface Invoker extends ScopedInvoker<NearInvocationScope> {


        /**
         * Gas Limits
         *
         * @deprecated migrate this to a configurable parameter
         */
        BigInteger DEFAULT_GAS_LIMIT = BigInteger.valueOf(6721975);

        /**
         * Sends a transaction to the blockchain.
         *
         * @param receiverId the id of the address to send the transactions to
         * @return the return value
         */
        default NearInvokeContractResponse send(final String receiverId) {
            return send(receiverId, List.of());
        }

        /**
         * Sends a transaction to the blockchain.
         *
         * @param receiverId the id of the address to send the transactions to
         * @param actions the actions to be performed by the transaction (see https://nomicon.io/RuntimeSpec/Actions)
         *                with their associated arguments
         * @return the return value
         */
        NearInvokeContractResponse send(String receiverId, List<Map<String, Map<String, List<?>>>> actions);

        /**
         * Calls a script on the blockchain.
         *
         * @param methodName the Cadence script to execute
         * @return the return value
         */
        default Object call(final String methodName) {
            return call(methodName, Collections.emptyList());
        }

        /**
         * Exectues a script, but does not write, to the blockchain.
         *
         * @param methodName the Cadence script to execute
         * @param arguments the arguments passed to the script itself
         * @return the return value
         */
        Object call(String methodName, List<?> arguments);

    }
}
