package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.BscSmartContractDao;
import com.namazustudios.socialengine.dao.BscTokenDao;
import com.namazustudios.socialengine.dao.BscWalletDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
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
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class UserBscSmartContractService implements BscSmartContractService {

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

        throw new ForbiddenException();
    }

    @Override
    public PendingOperation send(final EVMInvokeContractRequest invokeRequest,
                                 final Consumer<String> applicationLogConsumer,
                                 final Consumer<Throwable> exceptionConsumer) {

        throw new ForbiddenException();
    }

    @Override
    public PendingOperation call(final EVMInvokeContractRequest invokeRequest,
                                 final Consumer<String> applicationLogConsumer,
                                 final Consumer<Throwable> exceptionConsumer) {

        return doCall(invokeRequest, applicationLogConsumer, exceptionConsumer);
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

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

    @Inject
    private void setBscHost(@Named(Constants.BSC_RPC_PROVIDER)String bscHost) {
        this.bscHost = bscHost;
    }

    private String getBSCHost() {
        return this.bscHost;
    }
}
