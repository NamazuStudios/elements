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
import org.web3j.crypto.Credentials;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import javax.inject.Inject;
import java.math.BigInteger;
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
    public String  mintToken(final MintTokenRequest mintTokenRequest,
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

        return doInvoke(invokeContractRequest, (blockIndex, tx) -> {

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
    public String invoke(final InvokeContractRequest invokeRequest,
                                   final BiConsumer<Long, InvokeContractResponse> applicationLogConsumer,
                                   final Consumer<Throwable> exceptionConsumer) {
        return doInvoke(invokeRequest, (blockIndex, tx) -> {
            final var invokeContractResponse = new InvokeContractResponse();
            invokeContractResponse.setBlockNetworkId(blockIndex.toString());
        }, exceptionConsumer);
    }

    private String doInvoke(final InvokeContractRequest invokeRequest, final BiConsumer<Long, InvokeContractResponse> applicationLogConsumer,
                                      final Consumer<Throwable> exceptionConsumer) {
            final var contractMetadata = getBscSmartContractDao()
                .getBscSmartContract(invokeRequest.getContractId());

            if(!contractMetadata.getBlockchain().equals(BlockchainConstants.Names.BSC)) {
                final var msg = format("Contract Blockchain %s is not a supported type.", contractMetadata.getBlockchain());
                throw new ContractInvocationException(msg);
            }

            final var wallet = getBscWalletDao().getWallet(invokeRequest.getWalletId());
            final var mintAccount = wallet.getWallet().getAccounts().get(0);

            Credentials credentials = Credentials.create(Web3jWallet.decrypt(wallet.getWallet().getAccounts().get(0)));


            String contractAddress = contractMetadata.getBlockchain();

            final BigInteger gasPrice = BigInteger.valueOf(2205000);
            final BigInteger gasLimit = BigInteger.valueOf(14300000);
            int attempt = 200;
            int sleepDuration = 500;
            final ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);

            //Send a transaction to a (already deployed) smart contract
            try {
                // RawTransactionManager use a wallet (credential) to create and sign transaction
                TransactionManager manager = new RawTransactionManager(getBscw3JClient().getWeb3j(), credentials, attempt, sleepDuration);

                //load contract information into the transaction
                final BscSmartContract contract = BscSmartContract.load(contractAddress, getBscw3JClient().getWeb3j(), manager, gasProvider);

                //invoke contract function
                final var returnType = contract.run(invokeRequest.getMethodName()).send(); // <-- throws exception

                return  returnType;

            } catch(Exception e) {
                final var msg = format(
                        "Bsc Transaction Failed: %s",
                        e.getMessage()
                );

                throw new ContractInvocationException(msg);
            }
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
