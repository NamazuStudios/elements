package com.namazustudios.socialengine.service.blockchain.neo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.dao.NeoSmartContractDao;
import com.namazustudios.socialengine.dao.NeoTokenDao;
import com.namazustudios.socialengine.dao.NeoWalletDao;
import com.namazustudios.socialengine.exception.blockchain.ContractInvocationException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.model.blockchain.neo.MintNeoTokenResponse;
import com.namazustudios.socialengine.service.TopicService;
import com.namazustudios.socialengine.util.AsyncUtils;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.BlockchainConstants.MintStatus.*;
import static com.namazustudios.socialengine.rt.util.FinallyAction.logger;
import static java.lang.String.format;

public class SuperUserNeoSmartContractService implements NeoSmartContractService {

    private NeoSmartContractDao neoSmartContractDao;

    private NeoTokenDao neoTokenDao;

    private NeoWalletDao neoWalletDao;

    private Neow3jClient neow3JClient;

    private ObjectMapper objectMapper;

    private TopicService topicService;

    private final AsyncUtils asyncUtils = new AsyncUtils(SuperUserNeoSmartContractService.class);

    @Override
    public Pagination<ElementsSmartContract> getNeoSmartContracts(final int offset, final int count, final String search) {
        return getNeoSmartContractDao().getNeoSmartContracts(offset, count, search);
    }

    @Override
    public ElementsSmartContract getNeoSmartContract(final String contractIdOrName) {
        return getNeoSmartContractDao().getNeoSmartContract(contractIdOrName);
    }

    @Override
    public ElementsSmartContract patchNeoSmartContract(final PatchSmartContractRequest patchSmartContractRequest) {
        return getNeoSmartContractDao().patchNeoSmartContract(patchSmartContractRequest);
    }

    @Override
    public PendingOperation mintToken(final MintTokenRequest mintTokenRequest,
                                      final Consumer<MintNeoTokenResponse> tokenResponseConsumer,
                                      final Consumer<Throwable> exceptionConsumer) {

        final var tokenId = mintTokenRequest.getTokenId();
        final var neoToken = getNeoTokenDao().getToken(tokenId);

        final var clone = getNeoTokenDao().cloneNeoToken(neoToken);
        final var ownerAddress = mintTokenRequest.getOwnerAddress() != null ?
                mintTokenRequest.getOwnerAddress() : clone.getToken().getOwner();

        if(ownerAddress != null) {
            final var ownerHash = Account
                    .fromAddress(ownerAddress)
                    .getScriptHash();
            clone.getToken().setOwner(ownerHash.toString());
        }

        for (var stakeHolder : clone.getToken().getOwnership().getStakeHolders()) {
            final var stakeHolderHash = Account
                .fromAddress(stakeHolder.getOwner())
                .getScriptHash();
            stakeHolder.setOwner(stakeHolderHash.toString());
        }


        final var tokenMap = getObjectMapper().convertValue(clone.getToken(), Map.class);

        getNeoTokenDao().setMintStatusForToken(clone.getId(), MINT_PENDING);

        final var invokeContractRequest = new InvokeContractRequest();
        invokeContractRequest.setMethodName("mint");
        invokeContractRequest.setContractId(clone.getContractId());
        invokeContractRequest.setPassword(mintTokenRequest.getPassword());
        invokeContractRequest.setWalletId(mintTokenRequest.getWalletId());
        invokeContractRequest.setParameters(List.of(clone.getTokenUUID(), tokenMap));

        return doInvoke(invokeContractRequest, (blockIndex, tx) -> {

                    final var appLog = tx.getApplicationLog();

                    final var hasFault = appLog
                            .getExecutions()
                            .stream()
                            .anyMatch(e -> e.getState() == NeoVMStateType.FAULT);

                    if (hasFault) {
                        clone.setMintStatus(MINT_FAILED);
                    } else {
                        clone.setMintStatus(MINTED);
                    }

                    var fullyMinted = getNeoTokenDao().setMintStatusForToken(
                            clone.getId(),
                            clone.getMintStatus()
                    );

                    final var response = new MintNeoTokenResponse();
                    response.setToken(fullyMinted);
                    response.setBlockIndex(blockIndex);
                    tokenResponseConsumer.accept(response);

                },
                exceptionConsumer
        );
    }

    @Override
    public PendingOperation invoke(final InvokeContractRequest invokeRequest,
                                   final BiConsumer<Long, InvokeContractResponse> applicationLogConsumer,
                                   final Consumer<Throwable> exceptionConsumer) {
        return doInvoke(invokeRequest, (blockIndex, tx) -> {
            final var invokeContractResponse = new InvokeContractResponse();
            invokeContractResponse.setBlockNetworkId(blockIndex.toString());
            applicationLogConsumer.accept(blockIndex, invokeContractResponse);
        }, exceptionConsumer);
    }

    private PendingOperation doInvoke(final InvokeContractRequest invokeRequest,
                                      final BiConsumer<Long, Transaction> applicationLogConsumer,
                                      final Consumer<Throwable> exceptionConsumer) {
        return asyncUtils.doNoThrow(exceptionConsumer, () -> {

            final var contractMetadata = getNeoSmartContractDao()
                .getNeoSmartContract(invokeRequest.getContractId());

            if(!contractMetadata.getBlockchain().equals(BlockchainConstants.Names.NEO)) {
                final var msg = format("Contract Blockchain %s is not a supported type.", contractMetadata.getBlockchain());
                throw new ContractInvocationException(msg);
            }

            final var wallet = getNeoWalletDao().getWallet(invokeRequest.getWalletId());
            final var nepWallet = getNeow3JClient().elementsWalletToNEP6(wallet.getWallet());
            final var mintAccount = Wallet.fromNEP6Wallet(nepWallet).getDefaultAccount();
            final var smartContract = getNeow3JClient().getSmartContract(contractMetadata.getScriptHash());

            try {
                mintAccount.decryptPrivateKey(invokeRequest.getPassword());
            } catch (NEP2InvalidPassphrase | NEP2InvalidFormat | CipherException e) {
                throw new ContractInvocationException("Decrypting the account keys failed.", e);
            }

            try {

                final var params = invokeRequest.getParameters() == null ? null :
                    invokeRequest
                        .getParameters()
                        .stream()
                        .map(getNeow3JClient()::convertObject)
                        .toArray(ContractParameter[]::new);

                final var tx = smartContract
                    .invokeFunction(invokeRequest.getMethodName(), params)
                    .signers(AccountSigner.calledByEntry(mintAccount))
                    .sign();

                final var rawTx = tx.send();

                if(rawTx.hasError()) {

                    final var msg = format(
                        "Neo Transaction Failed: %s (Code %d). - %s",
                        rawTx.getError().getMessage(),
                        rawTx.getError().getCode(),
                        rawTx.getError().getData()
                    );

                    throw new ContractInvocationException(msg);

                }

                final var disposable = tx.track().subscribe(
                    blockIndex -> applicationLogConsumer.accept(blockIndex, tx),
                    exceptionConsumer::accept,
                    () -> logger.debug("Invocation Complete."));

                return disposable::dispose;

            } finally {
                asyncUtils.doNoThrowV(
                    exceptionConsumer,
                    () -> mintAccount.encryptPrivateKey(invokeRequest.getPassword())
                );
            }
        });
    }

    @Override
    public NeoInvokeFunction testInvoke(final InvokeContractRequest invokeRequest) {

        ElementsSmartContract contract = getNeoSmartContractDao().getNeoSmartContract(invokeRequest.getContractId());
        var wallet = getNeoWalletDao().getWallet(invokeRequest.getWalletId());
        var nepWallet = getNeow3JClient().elementsWalletToNEP6(wallet.getWallet());
        Account mintAccount = Wallet.fromNEP6Wallet(nepWallet).getDefaultAccount();
        try {
            mintAccount.decryptPrivateKey(invokeRequest.getPassword());
        } catch (NEP2InvalidPassphrase | NEP2InvalidFormat | CipherException e) {
            throw new ContractInvocationException("Decrypting the account keys failed: " + e);
        }

        switch(contract.getBlockchain()){
            case BlockchainConstants.Names.NEO:
                io.neow3j.contract.SmartContract smartContract = getNeow3JClient().getSmartContract(contract.getScriptHash());
                if (invokeRequest.getParameters() != null && invokeRequest.getParameters().size() > 0){
                    List<ContractParameter> invokeParams = new ArrayList<>();
                    for (var param : invokeRequest.getParameters()) {
                        invokeParams.add(getNeow3JClient().convertObject(param));
                    }
                    try {
                        var response = smartContract.callInvokeFunction(invokeRequest.getMethodName(), invokeParams, AccountSigner.calledByEntry(mintAccount));
                        mintAccount.encryptPrivateKey(invokeRequest.getPassword());
                        return response;
                    } catch (Throwable e){
                        try {
                            mintAccount.encryptPrivateKey(invokeRequest.getPassword());
                        } catch (CipherException er) {
                            throw new ContractInvocationException("Re-encrypting the account keys failed: " + er);
                        }
                        throw new ContractInvocationException("Invocation failed with exception: " + e);
                    }
                } else {
                    try {
                        var response = smartContract.callInvokeFunction(invokeRequest.getMethodName(), AccountSigner.calledByEntry(mintAccount));
                        mintAccount.encryptPrivateKey(invokeRequest.getPassword());
                        return response;
                    } catch (Throwable e){
                        try {
                            mintAccount.encryptPrivateKey(invokeRequest.getPassword());
                        } catch (CipherException er) {
                            throw new ContractInvocationException("Re-encrypting the account keys failed: " + er);
                        }
                        throw new ContractInvocationException("Invocation failed with exception: " + e);
                    }
                }
            default:
                try {
                    mintAccount.encryptPrivateKey(invokeRequest.getPassword());
                } catch (CipherException er) {
                    throw new ContractInvocationException("Re-encrypting the account keys failed: " + er);
                }
                throw new ContractInvocationException(format("Contract Blockchain %s is not a supported type.", contract.getBlockchain()));
        }
    }

    @Override
    public void deleteContract(String contractId) {
        getNeoSmartContractDao().deleteNeoSmartContract(contractId);
    }

    public NeoSmartContractDao getNeoSmartContractDao() {
        return neoSmartContractDao;
    }

    @Inject
    public void setNeoSmartContractDao(NeoSmartContractDao neoSmartContractDao) {
        this.neoSmartContractDao = neoSmartContractDao;
    }

    public NeoTokenDao getNeoTokenDao() {
        return neoTokenDao;
    }

    @Inject
    public void setNeoTokenDao(NeoTokenDao neoTokenDao) {
        this.neoTokenDao = neoTokenDao;
    }

    public NeoWalletDao getNeoWalletDao() {
        return neoWalletDao;
    }

    @Inject
    public void setNeoWalletDao(NeoWalletDao neoWalletDao) {
        this.neoWalletDao = neoWalletDao;
    }

    public Neow3jClient getNeow3JClient() {
        return neow3JClient;
    }

    @Inject
    public void setNeow3JClient(Neow3jClient neow3JClient) {
        this.neow3JClient = neow3JClient;
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
