package dev.getelements.elements.service.blockchain.invoke.evm;

import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.model.blockchain.contract.EVMInvokeContractResponse;
import dev.getelements.elements.model.blockchain.contract.EVMTransactionLog;
import dev.getelements.elements.service.EvmSmartContractInvocationService;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.utils.Numeric;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.web3j.abi.FunctionEncoder.encode;
import static org.web3j.abi.FunctionReturnDecoder.decode;

public class Web3jInvoker implements EvmSmartContractInvocationService.Invoker {

    private Web3j web3j;

    private EvmInvocationScope evmInvocationScope;

    @Override
    public void initialize(final EvmInvocationScope evmInvocationScope) {
        this.evmInvocationScope = evmInvocationScope;
    }

    @Override
    public Object call(
            final String method,
            final List<String> inputTypes,
            final List<Object> arguments,
            final List<String> outputTypes) {

        final var function = getFunction(method, inputTypes, arguments, outputTypes);

        final var transaction = Transaction.createEthCallTransaction(
                evmInvocationScope.getWalletAccount() == null
                        ? null
                        : evmInvocationScope.getWalletAccount().getAddress(),
                evmInvocationScope.getSmartContractAddress().getAddress(),
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
    public EVMInvokeContractResponse send(
            final String method,
            final List<String> inputTypes,
            final List<Object> arguments,
            final List<String> outputTypes) {

        if (evmInvocationScope.getWalletAccount().isEncrypted()) {
            throw new IllegalStateException("Wallet must be decrypted.");
        }

        final var function = getFunction(method, inputTypes, arguments, outputTypes);
        final var credentials = Credentials.create(evmInvocationScope.getWalletAccount().getPrivateKey());
        final var nonce = getNonce(credentials);

        final var rawTransaction = RawTransaction.createTransaction(
                nonce,
                evmInvocationScope.getGasPrice(),
                evmInvocationScope.getGasLimit(),
                evmInvocationScope.getSmartContractAddress().getAddress(),
                BigInteger.ZERO,
                encode(function)
        );

        final var signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        final var hexValue = Numeric.toHexString(signedMessage);

        final EthSendTransaction ethSendTransaction;

        try {
            ethSendTransaction = getWeb3j()
                    .ethSendRawTransaction(hexValue)
                    .send();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        final var transactionHash = ethSendTransaction.getTransactionHash();

        final var receiptProcessor = new PollingTransactionReceiptProcessor(
                getWeb3j(),
                TransactionManager.DEFAULT_POLLING_FREQUENCY,
                TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH
        );

        final TransactionReceipt txReceipt;

        try {
            txReceipt = receiptProcessor.waitForTransactionReceipt(transactionHash);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (TransactionException ex) {
            throw new InternalException(ex);
        }

        final var response = convertTransactionReceipt(txReceipt);

        if(response.getLogs() != null && response.getLogs().size() > 0) {

            final var data = response.getLogs().get(0).getData();

            final var decodedData = FunctionReturnDecoder.decode(data, function.getOutputParameters())
                    .stream()
                    .map(Type::getValue)
                    .collect(Collectors.toList());

            response.setDecodedLog(decodedData);

        }

        return response;

    }

    private EVMInvokeContractResponse convertTransactionReceipt(final TransactionReceipt txReceipt) {
        final var response = new EVMInvokeContractResponse();
        response.setBlockHash(txReceipt.getBlockHash());
        response.setBlockNumber(txReceipt.getBlockNumber().longValue());
        response.setContractAddress(txReceipt.getContractAddress());
        response.setCumulativeGasUsed(txReceipt.getCumulativeGasUsed().longValue());
        response.setEffectiveGasPrice(txReceipt.getEffectiveGasPrice());
        response.setFrom(txReceipt.getFrom());
        response.setGasUsed(txReceipt.getGasUsed().longValue());
        response.setLogs(txReceipt.getLogs().stream().map(this::convertLog).collect(Collectors.toList()));
        response.setLogsBloom(txReceipt.getLogsBloom());
        response.setRevertReason(txReceipt.getRevertReason());
        response.setRoot(txReceipt.getRoot());
        response.setStatus(txReceipt.getStatus() == null ? 1 : Numeric.decodeQuantity(txReceipt.getStatus()).longValue());
        response.setTo(txReceipt.getTo());
        response.setTransactionHash(txReceipt.getTransactionHash());
        response.setTransactionIndex(txReceipt.getTransactionIndex().longValue());
        response.setType(txReceipt.getType());
        return response;
    }

    private EVMTransactionLog convertLog(final Log l) {
        final var txLog = new EVMTransactionLog();
        txLog.setAddress(l.getAddress());
        txLog.setBlockHash(l.getBlockHash());
        txLog.setBlockNumber(l.getBlockNumber().longValue());
        txLog.setData(l.getData());
        txLog.setLogIndex(l.getLogIndex().longValue());
        txLog.setRemoved(l.isRemoved());
        txLog.setTopics(l.getTopics());
        txLog.setTransactionHash(l.getTransactionHash());
        txLog.setTransactionIndex(l.getTransactionIndex().longValue());
        txLog.setType(l.getType());
        return txLog;
    }

    private BigInteger getNonce(final Credentials credentials) {
        try {
            return getWeb3j()
                    .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                    .send()
                    .getTransactionCount();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Function getFunction(
            final String method,
            final List<String> inputTypes,
            final List<Object> arguments,
            final List<String> outputTypes) {
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

    public Web3j getWeb3j() {
        return web3j;
    }

    @Inject
    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }

}
