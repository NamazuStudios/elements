package com.namazustudios.socialengine.service.blockchain.evm;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContractAddress;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.blockchain.wallet.WalletAccount;
import com.namazustudios.socialengine.service.EvmSmartContractService;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;
import static org.web3j.abi.FunctionEncoder.encode;
import static org.web3j.abi.FunctionReturnDecoder.decode;

public class Web3jInvoker implements EvmSmartContractService.Invoker {

    private Web3j web3j;

    private Vault vault;

    private Wallet wallet;

    private WalletAccount walletAccount;

    private SmartContract smartContract;

    private BlockchainNetwork blockchainNetwork;

    private SmartContractAddress smartContractAddress;

    private BigInteger gasLimit;

    private BigInteger gasPrice;

    public void init(
            final Vault vault,
            final Wallet wallet,
            final SmartContract smartContract,
            final BlockchainNetwork blockchainNetwork,
            final SmartContractAddress smartContractAddress) {
        this.vault = vault;
        this.wallet = wallet;
        this.smartContract = smartContract;
        this.blockchainNetwork = blockchainNetwork;
        this.smartContractAddress = smartContractAddress;
    }

    @Override
    public Object call(
            final String method,
            final List<String> inputTypes,
            final List<Object> arguments,
            final List<String> outputTypes) {

        final var function = getFunction(method, inputTypes, arguments, outputTypes);

        final var transaction = Transaction.createEthCallTransaction(
                getWalletAccount() == null ? null : getWalletAccount().getAddress(),
                getSmartContractAddress().getAddress(),
                encode(function)
        );

        final EthCall call;

        try {
            call = getWeb3j()
                    .ethCall(transaction, DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new InternalException(ex);
        }

        return decode(call.getValue(), function.getOutputParameters())
                .stream()
                .map(Type::getValue)
                .collect(toList());

    }

    @Override
    public Object send(
            final String network,
            final String method,
            final List<String> inputTypes,
            final List<Object> arguments,
            final List<String> outputTypes) {

        if (getWalletAccount().isEncrypted()) {
            throw new IllegalStateException("Wallet must be decrypted.");
        }

        final var function = getFunction(method, inputTypes, arguments, outputTypes);
        final var credentials = Credentials.create(getWalletAccount().getPrivateKey());


        return null;
    }

    private Function getFunction(
            final String method,
            final List<String> inputTypes,
            final List<Object> arguments,
            final List<String> outputTypes) {

        final Function function;

        try {
            return FunctionEncoder.makeFunction(method, inputTypes, arguments, outputTypes);
        } catch (
                ClassNotFoundException |
                NoSuchMethodException |
                InstantiationException |
                IllegalAccessException |
                InvocationTargetException ex) {
            throw new InternalException(ex);
        }

    }

    public Vault getVault() {
        return vault;
    }

    public void setVault(Vault vault) {
        this.vault = vault;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public WalletAccount getWalletAccount() {
        return walletAccount;
    }

    public void setWalletAccount(WalletAccount walletAccount) {
        this.walletAccount = walletAccount;
    }

    public SmartContract getSmartContract() {
        return smartContract;
    }

    public void setSmartContract(SmartContract smartContract) {
        this.smartContract = smartContract;
    }

    public BlockchainNetwork getBlockchainNetwork() {
        return blockchainNetwork;
    }

    public void setBlockchainNetwork(BlockchainNetwork blockchainNetwork) {
        this.blockchainNetwork = blockchainNetwork;
    }

    public SmartContractAddress getSmartContractAddress() {
        return smartContractAddress;
    }

    public void setSmartContractAddress(SmartContractAddress smartContractAddress) {
        this.smartContractAddress = smartContractAddress;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    @Inject
    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

}
