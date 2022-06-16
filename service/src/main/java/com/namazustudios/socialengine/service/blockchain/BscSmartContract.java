package com.namazustudios.socialengine.service.blockchain;

import io.neow3j.protocol.Neow3jService;
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
 * with flexibility to customize function (method) calls
 */
public final class BscSmartContract extends Contract {
    /**
     * Contract binary key to generate new contract, the value is not used for now since contract will be loaded from deployed contract address
     * To be determined later
     */
    private static final String BINARY = "Bin file was not provided";

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
