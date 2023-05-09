package dev.getelements.elements.service;

import dev.getelements.elements.model.blockchain.contract.EVMInvokeContractResponse;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import dev.getelements.elements.service.blockchain.invoke.ScopedInvoker;
import dev.getelements.elements.service.blockchain.invoke.evm.EvmInvocationScope;

import java.math.BigInteger;
import java.util.List;

/**
 * A service which allows for the invocation of methods belonging to an EVM Smart Contract. Several blockchain networks
 * are EVM based even if the API is not ETH based. Therefore this service can handle the invocation details for the
 * EVM based networks.
 */
@Expose({
        @ModuleDefinition(
                value = "namazu.elements.service.smartcontract.evm"),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.smartcontract.evm",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )
})
public interface EvmSmartContractInvocationService extends SmartContractInvocationService<EvmSmartContractInvocationService.Invoker> {

    /**
     * Gas Price
     *
     * @deprecated migrate this to a configurable parameter
     */
   BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(20000000000L);

    /**
     * Gas Limits
     *
     * @deprecated migrate this to a configurable parameter
     */
    BigInteger DEFAULT_GAS_LIMIT = BigInteger.valueOf(6721975);

    /**
     * A type which performs the final invocation to the underlying smart contract.
     */
    interface Invoker extends ScopedInvoker<EvmInvocationScope> {

        /**
         * Calls the smart contract method with the supplied parameter types and return types.
         *
         * @param method the method name
         * @param inputTypes the parameter types
         * @param arguments the parameters themselves
         * @param outputTypes the return types
         * @return the return result of the call
         */
        Object call(
                String method,
                List<String> inputTypes,
                List<Object> arguments,
                List<String> outputTypes);

        /**
         * Sends a transaction to the blockchain by calling the specified smart contract method with the supplied
         * parameter types and return types.
         *
         * @param method the method name
         * @param inputTypes the parameter types
         * @param arguments the parameters themselves
         * @param outputTypes the return types
         * @return the return result of the call
         */
        EVMInvokeContractResponse send(
                String method,
                List<String> inputTypes,
                List<Object> arguments,
                List<String> outputTypes);

    }

}
