package dev.getelements.elements.service;

import dev.getelements.elements.model.blockchain.contract.NearInvokeContractResponse;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import dev.getelements.elements.service.blockchain.invoke.ScopedInvoker;
import dev.getelements.elements.service.blockchain.invoke.near.NearInvocationScope;

import java.util.Collections;
import java.util.List;

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
         * Sends a transaction to the blockchain.
         *
         * @param script the Cadence script to execute
         * @return the return value
         */
        default NearInvokeContractResponse send(final String script) {
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
        NearInvokeContractResponse send(String script, List<String> argumentTypes, List<?> arguments);

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
