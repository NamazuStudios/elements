package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.BscSmartContractDao;
import com.namazustudios.socialengine.dao.BscTokenDao;
import com.namazustudios.socialengine.dao.BscWalletDao;
import com.namazustudios.socialengine.exception.blockchain.ContractInvocationException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.EVMInvokeContractRequest;
import com.namazustudios.socialengine.model.blockchain.ElementsSmartContract;
import com.namazustudios.socialengine.model.blockchain.MintTokenRequest;
import com.namazustudios.socialengine.model.blockchain.PatchSmartContractRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.MintBscTokenResponse;
import com.namazustudios.socialengine.service.TopicService;
import com.namazustudios.socialengine.util.AsyncUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.utils.Numeric;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.BlockchainConstants.MintStatus.MINTED;
import static com.namazustudios.socialengine.BlockchainConstants.MintStatus.MINT_PENDING;
import static java.lang.String.format;

public class SuperUserBscSmartContractService implements BscSmartContractService {

    private BscSmartContractDao bscSmartContractDao;

    private BscTokenDao bscTokenDao;

    private BscWalletDao bscWalletDao;

    private Bscw3jClient bscw3JClient;

    private ObjectMapper objectMapper;

    private TopicService topicService;

    private Client client;

    private String bscHost;

    private final AsyncUtils asyncUtils = new AsyncUtils(SuperUserBscSmartContractService.class);

    @Override
    public Pagination<ElementsSmartContract> getBscSmartContracts(final int offset, final int count, final String search) {
        return getBscSmartContractDao().getBscSmartContracts(offset, count, search);
    }

    @Override
    public ElementsSmartContract getBscSmartContract(final String contractIdOrName) {
        return getBscSmartContractDao().getBscSmartContract(contractIdOrName);
    }

    @Override
    public ElementsSmartContract patchBscSmartContract(final PatchSmartContractRequest patchSmartContractRequest) {
        return getBscSmartContractDao().patchBscSmartContract(patchSmartContractRequest);
    }

    @Override
    public PendingOperation mintToken(final MintTokenRequest mintTokenRequest,
                            final Consumer<MintBscTokenResponse> tokenResponseConsumer,
                            final Consumer<Throwable> exceptionConsumer) {

        final var tokenId = mintTokenRequest.getTokenId();
        final var bscToken = getBscTokenDao().getToken(tokenId);

        final var clone = getBscTokenDao().cloneBscToken(bscToken);
        final var tokenMap = getObjectMapper().convertValue(clone.getToken(), Map.class);

        getBscTokenDao().setMintStatusForToken(clone.getId(), MINT_PENDING);

        final var invokeContractRequest = new EVMInvokeContractRequest();
        invokeContractRequest.setMethodName("mint");
        invokeContractRequest.setContractId(clone.getContractId());
        invokeContractRequest.setPassword(mintTokenRequest.getPassword());
        invokeContractRequest.setWalletId(mintTokenRequest.getWalletId());
        invokeContractRequest.setParameters(List.of(clone.getTokenUUID(), tokenMap));

        return doSend(invokeContractRequest, (result) -> {

                    clone.setMintStatus(MINTED);

                    var fullyMinted = getBscTokenDao().setMintStatusForToken(
                            clone.getId(),
                            clone.getMintStatus()
                    );

                    final var response = new MintBscTokenResponse();
                    response.setToken(fullyMinted);
                    tokenResponseConsumer.accept(response);

                },
                exceptionConsumer
        );
    }

    @Override
    public PendingOperation send(final EVMInvokeContractRequest invokeRequest,
                         final Consumer<String> applicationLogConsumer,
                         final Consumer<Throwable> exceptionConsumer) {

        return doSend(invokeRequest, applicationLogConsumer, exceptionConsumer);
    }

    @Override
    public PendingOperation call(final EVMInvokeContractRequest invokeRequest,
                                 final Consumer<String> applicationLogConsumer,
                                 final Consumer<Throwable> exceptionConsumer) {

        return doCall(invokeRequest, applicationLogConsumer, exceptionConsumer);
    }


    private PendingOperation doSend(final EVMInvokeContractRequest invokeRequest,
                            final Consumer<String> applicationLogConsumer,
                            final Consumer<Throwable> exceptionConsumer) {

        return asyncUtils.doNoThrow(exceptionConsumer, () -> {

            final var contractMetadata = getBscSmartContractDao()
                    .getBscSmartContract(invokeRequest.getContractId());

            if (!contractMetadata.getBlockchain().equals(BlockchainConstants.Names.BSC)) {
                final var msg = format("Contract Blockchain %s is not a supported type.", contractMetadata.getBlockchain());
                throw new ContractInvocationException(msg);
            }

            final var walletId = invokeRequest.getWalletId() == null ? contractMetadata.getWalletId() : invokeRequest.getWalletId();
            final var wallet = getBscWalletDao().getWallet(walletId);
            final var mintAccount = wallet.getWallet().getAccounts().get(0);
            final var decryptedAccount = getBscw3JClient().decrypt(wallet.getWallet(), mintAccount, null);
            final var credentials = Credentials.create(decryptedAccount);
            final var contractAddress = contractMetadata.getScriptHash();

            //Send a transaction to a (already deployed) smart contract
            try {

                final var function =
                        FunctionEncoder.makeFunction(
                                invokeRequest.getMethodName(),
                                invokeRequest.getInputTypes(),
                                invokeRequest.getParameters(),
                                invokeRequest.getOutputTypes());

                final var encodedFunction = FunctionEncoder.encode(function);

                final var ethGetTransactionCount =
                        getBscw3JClient().getWeb3j()
                                .ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                                .send();

                final var nonce = ethGetTransactionCount.getTransactionCount();

                // Prepare the rawTransaction
                final var rawTransaction = RawTransaction.createTransaction(
                        nonce,
                        BlockchainConstants.SmartContracts.GAS_PRICE,
                        BlockchainConstants.SmartContracts.GAS_LIMIT,
                        contractAddress,
                        encodedFunction);

                // Sign the transaction
                final var signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                final var hexValue = Numeric.toHexString(signedMessage);

                // Send transaction
                final var ethSendTransaction = getBscw3JClient().getWeb3j().ethSendRawTransaction(hexValue).send();
                final var transactionHash = ethSendTransaction.getTransactionHash();

                final var receiptProcessor = new PollingTransactionReceiptProcessor(
                        getBscw3JClient().getWeb3j(),
                        TransactionManager.DEFAULT_POLLING_FREQUENCY,
                        TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH);

                //Wait for the transaction block
                final var txReceipt = receiptProcessor.waitForTransactionReceipt(transactionHash);

                final var response = txReceipt.getRevertReason() != null ?
                        txReceipt.getRevertReason() :
                        txReceipt.getBlockHash();

                applicationLogConsumer.accept(response);

            } catch (Exception e) {
                final var msg = format(
                        "Bsc Transaction Failed: %s",
                        e.getMessage()
                );

                throw new ContractInvocationException(msg);
            }

            return null;
        });
    }

    private PendingOperation doCall(final EVMInvokeContractRequest invokeRequest,
                                    final Consumer<String> applicationLogConsumer,
                                    final Consumer<Throwable> exceptionConsumer) {

        return asyncUtils.doNoThrow(exceptionConsumer, () -> {

            final var contractMetadata = getBscSmartContractDao()
                    .getBscSmartContract(invokeRequest.getContractId());

            if (!contractMetadata.getBlockchain().equals(BlockchainConstants.Names.BSC)) {
                final var msg = format("Contract Blockchain %s is not a supported type.", contractMetadata.getBlockchain());
                throw new ContractInvocationException(msg);
            }

            final var walletId = invokeRequest.getWalletId() == null ? contractMetadata.getWalletId() : invokeRequest.getWalletId();
            final var wallet = getBscWalletDao().getWallet(walletId);
            final var mintAccount = wallet.getWallet().getAccounts().get(0);
            final var decryptedAccount = getBscw3JClient().decrypt(wallet.getWallet(), mintAccount, null);
            final var credentials = Credentials.create(decryptedAccount);
            final var contractAddress = contractMetadata.getScriptHash();

            //Send a transaction to a (already deployed) smart contract
            try {

                final var solidityOutputTypes =
                    invokeRequest.getOutputTypes().stream()
                        .map(o -> {
                            try {
                                return (TypeReference<Type>)TypeReference.makeTypeReference(o);
                            } catch (ClassNotFoundException e) {
                                throw new ContractInvocationException(e.getMessage());
                            }
                        })
                        .collect(Collectors.toUnmodifiableList());

                final var function =
                        FunctionEncoder.makeFunction(invokeRequest.getMethodName(),
                                invokeRequest.getInputTypes(),
                                invokeRequest.getParameters(),
                                invokeRequest.getOutputTypes());

                final var responseValue = callSmartContractFunction(function, credentials.getAddress(), contractAddress);
                final var responseObjects = FunctionReturnDecoder.decode(responseValue, solidityOutputTypes);
                final var response = responseObjects.get(0).getValue().toString();

                applicationLogConsumer.accept(response);

            } catch (Exception e) {

                final var msg = format(
                    "Bsc Call Failed: %s",
                    e.getMessage()
                );

                throw new ContractInvocationException(msg);

            }

            return null;
        });
    }

    private String callSmartContractFunction(Function function, String fromAddress, String contractAddress)
            throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);

        org.web3j.protocol.core.methods.response.EthCall response =
                getBscw3JClient().getWeb3j().ethCall(
                                Transaction.createEthCallTransaction(
                                        fromAddress, contractAddress, encodedFunction),
                                DefaultBlockParameterName.LATEST)
                        .sendAsync()
                        .get();

        return response.getValue();
    }

    @Override
    public void deleteContract(String contractId) {
        getBscSmartContractDao().deleteBscSmartContract(contractId);
    }

    public BscSmartContractDao getBscSmartContractDao() {
        return bscSmartContractDao;
    }

    @Inject
    public void setBscSmartContractDao(BscSmartContractDao bscSmartContractDao) {
        this.bscSmartContractDao = bscSmartContractDao;
    }

    public BscTokenDao getBscTokenDao() {
        return bscTokenDao;
    }

    @Inject
    public void setBscTokenDao(BscTokenDao bscTokenDao) {
        this.bscTokenDao = bscTokenDao;
    }

    public BscWalletDao getBscWalletDao() {
        return bscWalletDao;
    }

    @Inject
    public void setBscWalletDao(BscWalletDao bscWalletDao) {
        this.bscWalletDao = bscWalletDao;
    }

    public Bscw3jClient getBscw3JClient() {
        return bscw3JClient;
    }

    @Inject
    public void setBscw3JClient(Bscw3jClient bscw3JClient) {
        this.bscw3JClient = bscw3JClient;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TopicService getTopicService() {
        return topicService;
    }

    @Inject
    public void setTopicService(TopicService topicService) {
        this.topicService = topicService;
    }

}
