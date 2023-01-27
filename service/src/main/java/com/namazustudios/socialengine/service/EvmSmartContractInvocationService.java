package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.EVMInvokeContractResponse;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;
import com.namazustudios.socialengine.service.blockchain.invoke.ScopedInvoker;
import com.namazustudios.socialengine.service.blockchain.invoke.evm.EvmInvocationScope;

import java.math.BigInteger;
import java.util.List;

@Expose({
        @ModuleDefinition(
                value = "namazu.elements.service.smartcontract.evm"),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.smartcontract.evm",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )
})
/**
 * A service which allows for the invocation of methods belonging to an EVM Smart Contract. Several blockchain networks
 * are EVM based even if the API is not ETH based. Therefore this service can handle the invocation details for the
 * EVM based networks.
 */
public interface EvmSmartContractInvocationService extends SmartContractInvocationService<EvmSmartContractInvocationService.Invoker> {

    String IOC_NAME = "namazu.elements.service.smartcontract.evm";

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
