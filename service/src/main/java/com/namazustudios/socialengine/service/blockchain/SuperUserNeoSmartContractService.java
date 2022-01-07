package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.dao.NeoSmartContractDao;
import com.namazustudios.socialengine.dao.NeoTokenDao;
import com.namazustudios.socialengine.dao.NeoWalletDao;
import com.namazustudios.socialengine.exception.blockchain.ContractInvocationException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.model.blockchain.neo.NeoToken;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.types.ContractParameter;
import io.neow3j.wallet.Account;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SuperUserNeoSmartContractService implements NeoSmartContractService {

    private NeoSmartContractDao neoSmartContractDao;

    private NeoTokenDao neoTokenDao;

    private NeoWalletDao neoWalletDao;

    private Neow3jClient neow3JClient;

    private ObjectMapper objectMapper;

    @Override
    public Pagination<SmartContract> getNeoSmartContracts(int offset, int count, String search) {
        return getNeoSmartContractDao().getNeoSmartContracts(offset, count, search);
    }

    @Override
    public SmartContract getNeoSmartContract(String contractIdOrName) {
        return getNeoSmartContractDao().getNeoSmartContract(contractIdOrName);
    }

    @Override
    public SmartContract patchNeoSmartContract(PatchSmartContractRequest patchSmartContractRequest) {
        return getNeoSmartContractDao().patchNeoSmartContract(patchSmartContractRequest);
    }

    @Override
    public NeoSendRawTransaction mintToken(MintTokenRequest mintTokenRequest) {
        Account mintAccount = Account.fromAddress(mintTokenRequest.getAddress());
        for (var tid : mintTokenRequest.getTokenIds()) {
            var token = getNeoTokenDao().getToken(tid);
            if (token.getTotalMintedQuantity() < token.getToken().getTotalSupply()) {
                SmartContract contract = getNeoSmartContractDao().getNeoSmartContract(token.getContractId());
                var tokenClone = getNeoTokenDao().cloneNeoToken(token);

                switch (contract.getBlockchain()) {
                    case "NEO":
                        try {
                            var tkn = tokenClone.getToken();
                            io.neow3j.contract.SmartContract smartContract = getNeow3JClient().getSmartContract(contract.getScriptHash());
                            var ownerHash = Account.fromAddress(tkn.getOwner()).getScriptHash();
                            var tokenIdParam = ContractParameter.string(tokenClone.getTokenUUID());
                            tkn.setOwner(ownerHash.toString());
                            for (var stkhldr : tkn.getOwnership().getStakeHolders()) {
                                var stkhldrHash = Account.fromAddress(stkhldr.getOwner()).getScriptHash();
                                stkhldr.setOwner(stkhldrHash.toString());
                            }
                            var tokenMapParam = getNeow3JClient().convertObject(getObjectMapper().convertValue(tkn, Map.class));
                            getNeoTokenDao().setMintStatusForToken(tokenClone.getId(), NeoToken.MintStatus.MINT_PENDING);
                            var response = smartContract.invokeFunction("mint", tokenIdParam, tokenMapParam)
                                    .signers(AccountSigner.calledByEntry(mintAccount))
                                    .sign()
                                    .send();
                            if (!response.hasError()) {
                                getNeoTokenDao().setMintStatusForToken(tokenClone.getId(), NeoToken.MintStatus.MINTED);
                            } else {
                                getNeoTokenDao().setMintStatusForToken(tokenClone.getId(), NeoToken.MintStatus.MINT_FAILED);
                                throw new ContractInvocationException("Minting failed with error: " + response.getError().toString());
                            }
                        } catch (Throwable e) {
                            getNeoTokenDao().setMintStatusForToken(tokenClone.getId(), NeoToken.MintStatus.MINT_FAILED);
                            throw new ContractInvocationException("Minting failed with exception: " + e);
                        }
                    default:
                        throw new ContractInvocationException(String.format("Contract Blockchain %s is not a supported type.", contract.getBlockchain()));
                }
            } else {
                throw new ContractInvocationException(String.format("The token %s is out of supply. Create a new definition, or add to the total supply, to mint more.", token.getId()));
            }
        }
        throw new ContractInvocationException("Minting failed. Maybe your token list is empty?");
    }

    @Override
    public NeoSendRawTransaction invoke(InvokeContractRequest invokeRequest) {
        SmartContract contract = getNeoSmartContractDao().getNeoSmartContract(invokeRequest.getContractId());

        switch(contract.getBlockchain()){
            case "NEO":
                io.neow3j.contract.SmartContract smartContract = getNeow3JClient().getSmartContract(contract.getScriptHash());
                Account account = Account.fromAddress(invokeRequest.getAddress());
                if (invokeRequest.getParameters().size() > 0){
                    List<ContractParameter> invokeParams = new ArrayList<>();
                    for (var param : invokeRequest.getParameters()) {
                        invokeParams.add(getNeow3JClient().convertObject(param));
                    }
                    try {
                        return smartContract.invokeFunction(invokeRequest.getMethodName(), invokeParams.toArray(new ContractParameter[0]))
                                .signers(AccountSigner.calledByEntry(account))
                                .sign()
                                .send();
                    } catch (Throwable e){
                        throw new ContractInvocationException("Invocation failed with exception: " + e);
                    }
                } else {
                    try {
                        return smartContract.invokeFunction(invokeRequest.getMethodName()).signers(AccountSigner.calledByEntry(account))
                                .sign()
                                .send();
                    } catch (Throwable e){
                        throw new ContractInvocationException("Invocation failed with exception: " + e);
                    }
                }
            default:
                throw new ContractInvocationException(String.format("Contract Blockchain %s is not a supported type.", contract.getBlockchain()));
        }
    }

    @Override
    public NeoInvokeFunction testInvoke(InvokeContractRequest invokeRequest) {
        SmartContract contract = getNeoSmartContractDao().getNeoSmartContract(invokeRequest.getContractId());

        switch(contract.getBlockchain()){
            case "NEO":
                io.neow3j.contract.SmartContract smartContract = getNeow3JClient().getSmartContract(contract.getScriptHash());
                Account account = Account.fromAddress(invokeRequest.getAddress());
                if (invokeRequest.getParameters().size() > 0){
                    List<ContractParameter> invokeParams = new ArrayList<>();
                    for (var param : invokeRequest.getParameters()) {
                        invokeParams.add(getNeow3JClient().convertObject(param));
                    }
                    try {
                        return smartContract.callInvokeFunction(invokeRequest.getMethodName(), invokeParams, AccountSigner.calledByEntry(account));
                    } catch (Throwable e){
                        throw new ContractInvocationException("Invocation failed with exception: " + e);
                    }
                } else {
                    try {
                        return smartContract.callInvokeFunction(invokeRequest.getMethodName(), AccountSigner.calledByEntry(account));
                    } catch (Throwable e){
                        throw new ContractInvocationException("Invocation failed with exception: " + e);
                    }
                }
            default:
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
}
