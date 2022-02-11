package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.dao.NeoSmartContractDao;
import com.namazustudios.socialengine.dao.NeoTokenDao;
import com.namazustudios.socialengine.dao.NeoWalletDao;
import com.namazustudios.socialengine.exception.blockchain.ContractInvocationException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.ElementsSmartContract;
import com.namazustudios.socialengine.model.blockchain.InvokeContractRequest;
import com.namazustudios.socialengine.model.blockchain.MintTokenRequest;
import com.namazustudios.socialengine.model.blockchain.PatchSmartContractRequest;
import com.namazustudios.socialengine.model.blockchain.neo.NeoToken;
import com.namazustudios.socialengine.service.TopicService;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.core.response.NeoApplicationLog;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.types.NeoVMStateType;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.exceptions.AccountStateException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.BlockchainConstants.MintStatus.*;
import static com.namazustudios.socialengine.rt.util.FinallyAction.logger;

public class SuperUserNeoSmartContractService implements NeoSmartContractService {

    private NeoSmartContractDao neoSmartContractDao;

    private NeoTokenDao neoTokenDao;

    private NeoWalletDao neoWalletDao;

    private Neow3jClient neow3JClient;

    private ObjectMapper objectMapper;

    private TopicService topicService;

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
    public void mintToken(final MintTokenRequest mintTokenRequest,
                          final Consumer<NeoToken> applicationLogConsumer,
                          final Consumer<Throwable> exceptionConsumer) {

        var consumeAndLog = exceptionConsumer.andThen(ex -> logger.error("Minting Error.", ex));

        final var tokenId = mintTokenRequest.getTokenId();
        final var token = getNeoTokenDao().getToken(tokenId);
        final var contract = getNeoSmartContractDao().getNeoSmartContract(token.getContractId());

        if(!contract.getBlockchain().equals(BlockchainConstants.Names.NEO)) {
            consumeAndLog.accept(new ContractInvocationException(String.format("Contract Blockchain %s is not a supported type.", contract.getBlockchain())));
            return;
        }

        if (token.getTotalMintedQuantity() >= token.getToken().getTotalSupply()) {
            consumeAndLog.accept(new ContractInvocationException(String.format("The token %s is out of supply. Create a new definition, or add to the total supply, to mint more.", token.getId())));
            return;
        }

        doAsync(consumeAndLog, () -> {

            final var wallet = getNeoWalletDao().getWallet(mintTokenRequest.getWalletId());
            final var nepWallet = getNeow3JClient().elementsWalletToNEP6(wallet.getWallet());
            final var mintAccount = Wallet.fromNEP6Wallet(nepWallet).getDefaultAccount();

            try {
                mintAccount.decryptPrivateKey(mintTokenRequest.getPassword());
            } catch (NEP2InvalidPassphrase | NEP2InvalidFormat | CipherException e) {
                consumeAndLog.accept(new ContractInvocationException("Decrypting the account keys failed: " + e));
            }

            final var tokenClone = getNeoTokenDao().cloneNeoToken(token);

            try {

                final var tkn = tokenClone.getToken();
                final var smartContract = getNeow3JClient().getSmartContract(contract.getScriptHash());
                final var ownerHash = Account.fromAddress(tkn.getOwner()).getScriptHash();
                final var tokenIdParam = ContractParameter.string(tokenClone.getTokenUUID());
                tkn.setOwner(ownerHash.toString());

                for (var stakeHolder : tkn.getOwnership().getStakeHolders()) {
                    final var stakeHolderHash = Account
                            .fromAddress(stakeHolder.getOwner())
                            .getScriptHash();
                    stakeHolder.setOwner(stakeHolderHash.toString());
                }

                final var tokenMap = getObjectMapper().convertValue(tkn, Map.class);
                final var tokenMapParam = getNeow3JClient().convertObject(tokenMap);
                getNeoTokenDao().setMintStatusForToken(tokenClone.getId(), MINT_PENDING);

                final var tx = smartContract
                        .invokeFunction("mint", tokenIdParam, tokenMapParam)
                        .signers(AccountSigner.calledByEntry(mintAccount))
                        .sign();

                final var rawTx = tx.send();

                if(rawTx.hasError()) {
                    consumeAndLog.accept(new ContractInvocationException("Minting failed with exception: " + rawTx.getError().getMessage()));
                } else {

                    tx.track().subscribe(blockIndex -> doAsync(consumeAndLog, () -> {

                        final var appLog = tx.getApplicationLog();

                        final var hasFault = appLog
                                .getExecutions()
                                .stream()
                                .anyMatch(e -> e.getState() == NeoVMStateType.FAULT);

                        if (hasFault) {
                            tokenClone.setMintStatus(MINT_FAILED);
                        } else {
                            tokenClone.setMintStatus(MINTED);
                        }

                        getNeoTokenDao().setMintStatusForToken(tokenClone.getId(), tokenClone.getMintStatus());

                        applicationLogConsumer.accept(tokenClone);
                    }),
                    consumeAndLog::accept,
                    () -> logger.debug("Completed for token {}.", tokenClone.getId()));
                }
            } catch (Throwable e) {
                getNeoTokenDao().setMintStatusForToken(tokenClone.getId(), MINT_FAILED);
                consumeAndLog.accept(new ContractInvocationException("Minting failed with exception: " + e));
            }
            finally {
                try {
                    mintAccount.encryptPrivateKey(mintTokenRequest.getPassword());
                } catch (CipherException | AccountStateException er) {
                    consumeAndLog.accept(new ContractInvocationException("Re-encrypting the account keys failed: " + er));
                }
            }

        });
    }

    @Override
    public void invoke(final InvokeContractRequest invokeRequest,
                       final Consumer<NeoApplicationLog> applicationLogConsumer,
                       final Consumer<Throwable> exceptionConsumer) {
        
        var consumeAndLog = exceptionConsumer.andThen(ex -> logger.error("Invocation error.", ex));

        doAsync(consumeAndLog, () -> {

            final var contract = getNeoSmartContractDao().getNeoSmartContract(invokeRequest.getContractId());
            final var wallet = getNeoWalletDao().getWallet(invokeRequest.getWalletId());
            final var nepWallet = getNeow3JClient().elementsWalletToNEP6(wallet.getWallet());
            final var mintAccount = Wallet.fromNEP6Wallet(nepWallet).getDefaultAccount();

            try {
                mintAccount.decryptPrivateKey(invokeRequest.getPassword());
            } catch (NEP2InvalidPassphrase | NEP2InvalidFormat | CipherException e) {
                var ex = new ContractInvocationException("Decrypting the account keys failed: " + e);
                consumeAndLog.accept(ex);
                throw ex;
            }

            switch(contract.getBlockchain()) {

                case BlockchainConstants.Names.NEO:

                    List<ContractParameter> invokeParams = null;

                    if (invokeRequest.getParameters().size() > 0) {

                        invokeParams = new ArrayList<>();

                        for (var param : invokeRequest.getParameters()) {
                            invokeParams.add(getNeow3JClient().convertObject(param));
                        }
                    }

                    final byte[] script = new ScriptBuilder()
                            .contractCall(new Hash160(contract.getScriptHash()), invokeRequest.getMethodName(), invokeParams)
                            .toArray();

                    try {

                        final var tx = new TransactionBuilder(getNeow3JClient().getNeow3j())
                                .script(script)
                                .signers(AccountSigner.calledByEntry(mintAccount))
                                .sign();

                        final var rawTx = tx.send();

                        if(rawTx.hasError()) {
                            consumeAndLog.accept(new ContractInvocationException("Minting failed with exception: " + rawTx.getError().getMessage()));
                        } else {
                            tx.track().subscribe(
                                blockIndex -> applicationLogConsumer.accept(tx.getApplicationLog()),
                                consumeAndLog::accept,
                                () -> logger.debug("Completed."));
                        }

                    } catch (Throwable e) {
                        var ex = new ContractInvocationException("Invocation failed with exception: " + e);
                        consumeAndLog.accept(ex);
                    } finally {
                        try {
                            mintAccount.encryptPrivateKey(invokeRequest.getPassword());
                        } catch (CipherException er) {
                            var ex = new ContractInvocationException("Re-encrypting the account keys failed: " + er);
                            consumeAndLog.accept(ex);
                        }
                    }

                    break;
                default:

                    try {
                        mintAccount.encryptPrivateKey(invokeRequest.getPassword());
                    } catch (CipherException er) {
                        var ex = new ContractInvocationException("Re-encrypting the account keys failed: " + er);
                        consumeAndLog.accept(ex);
                    }

                    var ex = new ContractInvocationException(String.format("Contract Blockchain %s is not a supported type.", contract.getBlockchain()));
                    consumeAndLog.accept(ex);
                    break;

            }
        });
    }

    private void doAsync(final Consumer<Throwable> exceptionConsumer, final Runnable operation) {
        try {
            operation.run();
        } catch (Exception ex) {
            exceptionConsumer.accept(ex);
        }
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
                throw new ContractInvocationException(String.format("Contract Blockchain %s is not a supported type.", contract.getBlockchain()));
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
