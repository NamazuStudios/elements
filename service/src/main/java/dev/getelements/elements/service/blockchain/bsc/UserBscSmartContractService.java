package dev.getelements.elements.service.blockchain.bsc;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.BlockchainConstants;
import dev.getelements.elements.Constants;
import dev.getelements.elements.dao.BscSmartContractDao;
import dev.getelements.elements.dao.BscTokenDao;
import dev.getelements.elements.dao.BscWalletDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.exception.blockchain.ContractInvocationException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.*;
import dev.getelements.elements.model.blockchain.bsc.MintBscTokenResponse;
import dev.getelements.elements.model.blockchain.contract.EVMInvokeContractRequest;
import dev.getelements.elements.model.blockchain.contract.EVMInvokeContractResponse;
import dev.getelements.elements.service.TopicService;
import dev.getelements.elements.util.AsyncUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import java.util.List;
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
                                 final Consumer<EVMInvokeContractResponse> applicationLogConsumer,
                                 final Consumer<Throwable> exceptionConsumer) {

        throw new ForbiddenException();
    }

    @Override
    public PendingOperation call(final EVMInvokeContractRequest invokeRequest,
                                 final Consumer<List<Object>> applicationLogConsumer,
                                 final Consumer<Throwable> exceptionConsumer) {

        return doCall(invokeRequest, applicationLogConsumer, exceptionConsumer);
    }

    private PendingOperation doCall(final EVMInvokeContractRequest invokeRequest,
                                    final Consumer<List<Object>> applicationLogConsumer,
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
            final var accountAddress = wallet.getWallet().getAddresses().get(0);
            final var contractAddress = contractMetadata.getScriptHash();

            //Send a transaction to a (already deployed) smart contract
            try {

                final var function =
                        FunctionEncoder.makeFunction(invokeRequest.getMethodName(),
                                invokeRequest.getInputTypes(),
                                invokeRequest.getParameters(),
                                invokeRequest.getOutputTypes());

                final var encodedFunction = FunctionEncoder.encode(function);

                final var transaction =
                        Transaction.createEthCallTransaction(accountAddress, contractAddress, encodedFunction);

                final var ethCall = getBscw3JClient().getWeb3j()
                        .ethCall(transaction, DefaultBlockParameterName.LATEST)
                        .sendAsync()
                        .get();

                final var responseValue = ethCall.getValue();

                final var responseObjects =
                        FunctionReturnDecoder.decode(responseValue, function.getOutputParameters());

                final var convertedObjects = responseObjects
                        .stream()
                        .map(o -> o.getValue())
                        .collect(Collectors.toList());

                applicationLogConsumer.accept(convertedObjects);

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
