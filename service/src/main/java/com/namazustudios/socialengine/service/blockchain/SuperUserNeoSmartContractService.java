package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.dao.NeoSmartContractDao;
import com.namazustudios.socialengine.dao.NeoTokenDao;
import com.namazustudios.socialengine.dao.NeoWalletDao;
import com.namazustudios.socialengine.exception.blockchain.ContractInvocationException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.service.Topic;
import com.namazustudios.socialengine.service.TopicService;
import io.neow3j.contract.SmartContract;
import io.neow3j.crypto.exceptions.CipherException;
import io.neow3j.crypto.exceptions.NEP2InvalidFormat;
import io.neow3j.crypto.exceptions.NEP2InvalidPassphrase;
import io.neow3j.protocol.core.response.*;
import io.neow3j.script.ScriptBuilder;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.transaction.Transaction;
import io.neow3j.transaction.TransactionBuilder;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.Hash160;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.Wallet;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static java.util.concurrent.Executors.newScheduledThreadPool;

public class SuperUserNeoSmartContractService implements NeoSmartContractService {

    private NeoSmartContractDao neoSmartContractDao;

    private NeoTokenDao neoTokenDao;

    private NeoWalletDao neoWalletDao;

    private Neow3jClient neow3JClient;

    private ObjectMapper objectMapper;

    private TopicService topicService;

    @Override
    public Pagination<ElementsSmartContract> getNeoSmartContracts(int offset, int count, String search) {
        return getNeoSmartContractDao().getNeoSmartContracts(offset, count, search);
    }

    @Override
    public ElementsSmartContract getNeoSmartContract(String contractIdOrName) {
        return getNeoSmartContractDao().getNeoSmartContract(contractIdOrName);
    }

    @Override
    public ElementsSmartContract patchNeoSmartContract(PatchSmartContractRequest patchSmartContractRequest) {
        return getNeoSmartContractDao().patchNeoSmartContract(patchSmartContractRequest);
    }

    @Override
    public List<NeoSendRawTransaction> mintToken(MintTokenRequest mintTokenRequest) {

        var wallet = getNeoWalletDao().getWallet(mintTokenRequest.getWalletId());
        var nepWallet = getNeow3JClient().elementsWalletToNEP6(wallet.getWallet());
        var mintAccount = Wallet.fromNEP6Wallet(nepWallet).getDefaultAccount();
        var tokenIds = mintTokenRequest.getTokenIds();
        List<NeoSendRawTransaction> responses = new ArrayList<>();

        if(tokenIds.size() == 0) {
            return responses;
        }

        try {
            mintAccount.decryptPrivateKey(mintTokenRequest.getPassword());
        } catch (NEP2InvalidPassphrase | NEP2InvalidFormat | CipherException e) {
            throw new ContractInvocationException("Decrypting the account keys failed: " + e);
        }

        for (var tid : tokenIds) {

            var token = getNeoTokenDao().getToken(tid);

            if (token.getTotalMintedQuantity() < token.getToken().getTotalSupply()) {

                var contract = getNeoSmartContractDao().getNeoSmartContract(token.getContractId());
                var tokenClone = getNeoTokenDao().cloneNeoToken(token);

                switch (contract.getBlockchain()) {

                    case BlockchainConstants.Names.NEO:

                        try {
                            var tkn = tokenClone.getToken();
                            var smartContract = getNeow3JClient().getSmartContract(contract.getScriptHash());
                            var ownerHash = Account.fromAddress(tkn.getOwner()).getScriptHash();
                            var tokenIdParam = ContractParameter.string(tokenClone.getTokenUUID());

                            tkn.setOwner(ownerHash.toString());

                            for (var stkhldr : tkn.getOwnership().getStakeHolders()) {
                                var stkhldrHash = Account.fromAddress(stkhldr.getOwner()).getScriptHash();
                                stkhldr.setOwner(stkhldrHash.toString());
                            }

                            var tokenMapParam = getNeow3JClient().convertObject(getObjectMapper().convertValue(tkn, Map.class));
                            getNeoTokenDao().setMintStatusForToken(tokenClone.getId(), BlockchainConstants.MintStatus.MINT_PENDING);

                            var response = smartContract.invokeFunction("mint", tokenIdParam, tokenMapParam)
                                    .signers(AccountSigner.calledByEntry(mintAccount))
                                    .sign()
                                    .send();

                            if (!response.hasError()) {
                                getNeoTokenDao().setMintStatusForToken(tokenClone.getId(), BlockchainConstants.MintStatus.MINTED);
                                responses.add(response);
                            } else {
                                getNeoTokenDao().setMintStatusForToken(tokenClone.getId(), BlockchainConstants.MintStatus.MINT_FAILED);
                                throw new ContractInvocationException("Minting failed with error: " + response.getError().toString());
                            }

                        } catch (Throwable e) {

                            getNeoTokenDao().setMintStatusForToken(tokenClone.getId(), BlockchainConstants.MintStatus.MINT_FAILED);

                            throw new ContractInvocationException("Minting failed with exception: " + e);
                        } finally {
                            try {
                                mintAccount.encryptPrivateKey(mintTokenRequest.getPassword());
                            } catch (CipherException er) {
                                throw new ContractInvocationException("Re-encrypting the account keys failed: " + er);
                            }
                        }

                        break;

                    default:

                        try {
                            mintAccount.encryptPrivateKey(mintTokenRequest.getPassword());
                        } catch (CipherException er) {
                            throw new ContractInvocationException("Re-encrypting the account keys failed: " + er);
                        }

                        throw new ContractInvocationException(String.format("Contract Blockchain %s is not a supported type.", contract.getBlockchain()));
                }
            } else {
                try {
                    mintAccount.encryptPrivateKey(mintTokenRequest.getPassword());
                } catch (CipherException er) {
                    throw new ContractInvocationException("Re-encrypting the account keys failed: " + er);
                }
                throw new ContractInvocationException(String.format("The token %s is out of supply. Create a new definition, or add to the total supply, to mint more.", token.getId()));
            }
        }

        return responses;
    }

    @Override
    public void invoke(final InvokeContractRequest invokeRequest,
                       final Consumer<NeoApplicationLog> applicationLogConsumer,
                       final Consumer<Exception> exceptionConsumer) {

        final var contract = getNeoSmartContractDao().getNeoSmartContract(invokeRequest.getContractId());
        final var wallet = getNeoWalletDao().getWallet(invokeRequest.getWalletId());
        final var nepWallet = getNeow3JClient().elementsWalletToNEP6(wallet.getWallet());
        final var mintAccount = Wallet.fromNEP6Wallet(nepWallet).getDefaultAccount();

        try {
            mintAccount.decryptPrivateKey(invokeRequest.getPassword());
        } catch (NEP2InvalidPassphrase | NEP2InvalidFormat | CipherException e) {
            var ex = new ContractInvocationException("Decrypting the account keys failed: " + e);
            exceptionConsumer.accept(ex);
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

                    tx.track().subscribe(blockIndex -> {
                        applicationLogConsumer.accept(tx.getApplicationLog());
                    });

                    tx.send();

                } catch (Throwable e) {
                    var ex = new ContractInvocationException("Invocation failed with exception: " + e);
                    exceptionConsumer.accept(ex);
                } finally {
                    try {
                        mintAccount.encryptPrivateKey(invokeRequest.getPassword());
                    } catch (CipherException er) {
                        var ex = new ContractInvocationException("Re-encrypting the account keys failed: " + er);
                        exceptionConsumer.accept(ex);
                    }
                }

                break;
            default:

                try {
                    mintAccount.encryptPrivateKey(invokeRequest.getPassword());
                } catch (CipherException er) {
                    var ex = new ContractInvocationException("Re-encrypting the account keys failed: " + er);
                    exceptionConsumer.accept(ex);
                }

                var ex = new ContractInvocationException(String.format("Contract Blockchain %s is not a supported type.", contract.getBlockchain()));
                exceptionConsumer.accept(ex);

                break;
        }
    }

    @Override
    public NeoInvokeFunction testInvoke(InvokeContractRequest invokeRequest) {
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
                if (invokeRequest.getParameters().size() > 0){
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
