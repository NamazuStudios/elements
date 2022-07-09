package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.dao.BscSmartContractDao;
import com.namazustudios.socialengine.dao.BscTokenDao;
import com.namazustudios.socialengine.dao.BscWalletDao;
import com.namazustudios.socialengine.exception.blockchain.ContractInvocationException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.model.blockchain.bsc.MintBscTokenResponse;
import com.namazustudios.socialengine.model.blockchain.bsc.Web3jWallet;
import com.namazustudios.socialengine.service.TopicService;
import com.namazustudios.socialengine.util.AsyncUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.BlockchainConstants.MintStatus.*;
import static java.lang.String.format;

public class SuperUserBscSmartContractService implements BscSmartContractService {

    private BscSmartContractDao bscSmartContractDao;

    private BscTokenDao bscTokenDao;

    private BscWalletDao bscWalletDao;

    private Bscw3jClient bscw3JClient;

    private ObjectMapper objectMapper;

    private TopicService topicService;

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
        final var ownerAddress = mintTokenRequest.getOwnerAddress() != null ?
                mintTokenRequest.getOwnerAddress() : clone.getToken().getOwner();


        final var tokenMap = getObjectMapper().convertValue(clone.getToken(), Map.class);

        getBscTokenDao().setMintStatusForToken(clone.getId(), MINT_PENDING);

        final var invokeContractRequest = new InvokeContractRequest();
        invokeContractRequest.setMethodName("mint");
        invokeContractRequest.setContractId(clone.getContractId());
        invokeContractRequest.setPassword(mintTokenRequest.getPassword());
        invokeContractRequest.setWalletId(mintTokenRequest.getWalletId());
        invokeContractRequest.setParameters(List.of(clone.getTokenUUID(), tokenMap));

        return doInvoke(invokeContractRequest, (result) -> {

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
    public PendingOperation invoke(final InvokeContractRequest invokeRequest,
                         final Consumer<String> applicationLogConsumer,
                         final Consumer<Throwable> exceptionConsumer) {

        return doInvoke(invokeRequest, applicationLogConsumer, exceptionConsumer);
    }

    private PendingOperation doInvoke(final InvokeContractRequest invokeRequest,
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
            final var decryptedAccount = Web3jWallet.decrypt(mintAccount);
            final var credentials = Credentials.create(decryptedAccount);
            final var contractAddress = contractMetadata.getScriptHash();

            final int attempt = 200;
            final int sleepDuration = 500;
            final ContractGasProvider gasProvider = new StaticGasProvider(BlockchainConstants.SmartContracts.GAS_PRICE,
                    BlockchainConstants.SmartContracts.GAS_LIMIT);

            //Send a transaction to a (already deployed) smart contract
            try {
                // RawTransactionManager use a wallet (credential) to create and sign transaction
                final TransactionManager manager = new RawTransactionManager(getBscw3JClient().getWeb3j(), credentials, attempt, sleepDuration);

                //TODO: I think this is what we need to get working
//                final var tokenId = new TypeReference<Uint256>(){};
//
//                Function function = new Function(
//                        invokeRequest.getMethodName(),  // function we're calling
//                        invokeRequest.getParameters(),  // Parameters to pass as Solidity Types
//                        Arrays.asList(tokenId));

//                String encodedFunction = FunctionEncoder.encode(function)
//                Transaction transaction = Transaction.createFunctionCallTransaction(
//                        <from>, <gasPrice>, <gasLimit>, contractAddress, <funds>, encodedFunction);
//
//                org.web3j.protocol.core.methods.response.EthSendTransaction transactionResponse =
//                        web3j.ethSendTransaction(transaction).sendAsync().get();

                //String transactionHash = transactionResponse.getTransactionHash();

                // wait for response using EthGetTransactionReceipt...

                //load contract information into the transaction
                final var contract = BscSmartContract.load(contractAddress, getBscw3JClient().getWeb3j(), manager, gasProvider);

                //invoke contract function
                final var result = contract.run(invokeRequest.getMethodName()).send(); // <-- throws exception

                applicationLogConsumer.accept(result);

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
