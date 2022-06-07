package com.namazustudios.socialengine.service.blockchain;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * The class is the Wrapper of Web3j Contract
 * inherits Contract APIs to interact with Smart Contract
 * with flexibility customized function (method) calls
 */
public final class BscSmartContract extends Contract {
    private static final String BINARY = "6060604052341561000f57600080fd5b5b600160a060020a0333166000908152602081905260409020620f424090555b5b6101678061003f6000396000f300606060405263ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166327e235e38114610048578063a9059cbb14610086575b600080fd5b341561005357600080fd5b61007473ffffffffffffffffffffffffffffffffffffffff600435166100b7565b60405190815260200160405180910390f35b341561009157600080fd5b6100b573ffffffffffffffffffffffffffffffffffffffff600435166024356100c9565b005b60006020819052908152604090205481565b73ffffffffffffffffffffffffffffffffffffffff3316600090815260208190526040902054819010156100fc57600080fd5b73ffffffffffffffffffffffffffffffffffffffff338116600090815260208190526040808220805485900390559184168152208054820190555b50505600a165627a7a7230582081fd33c821a86127abf00c9fafe2e14e4db6279ab9dd788e3ad3597d2280b6cf0029";

    private BscSmartContract(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected BscSmartContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    private BscSmartContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public RemoteFunctionCall<String> run(String name) {
        final Function function = new Function(name,
                Arrays.<Type>asList(),
                Arrays.asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    @Deprecated
    public static RemoteCall<BscSmartContract> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(BscSmartContract.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<BscSmartContract> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(BscSmartContract.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<BscSmartContract> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(BscSmartContract.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }



    @Deprecated
    public static BscSmartContract load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new BscSmartContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static BscSmartContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new BscSmartContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }


    public static BscSmartContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new BscSmartContract(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<BscSmartContract> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(BscSmartContract.class, web3j, credentials, contractGasProvider, BINARY, "");
    }
}
