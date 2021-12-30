package com.namazustudios.socialengine.service.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.ArrayMap;
import com.namazustudios.socialengine.dao.NeoSmartContractDao;
import com.namazustudios.socialengine.dao.NeoTokenDao;
import com.namazustudios.socialengine.dao.NeoWalletDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.*;
import io.neow3j.contract.SmartContract;
import io.neow3j.protocol.core.response.NeoInvokeFunction;
import io.neow3j.protocol.core.response.NeoSendRawTransaction;
import io.neow3j.transaction.AccountSigner;
import io.neow3j.types.ContractParameter;
import io.neow3j.types.ContractParameterType;
import io.neow3j.wallet.Account;
import io.neow3j.wallet.nep6.NEP6Wallet;
import org.dozer.DozerBeanMapper;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SuperUserNeoSmartContractService implements NeoSmartContractService {

    private NeoSmartContractDao neoSmartContractDao;

    private NeoTokenDao neoTokenDao;

    private NeoWalletDao neoWalletDao;

    private Neow3jClient neow3JClient;

    private ObjectMapper objectMapper;

    @Override
    public Pagination<NeoSmartContract> getNeoSmartContracts(int offset, int count, String search) {
        return getNeoSmartContractDao().getNeoSmartContracts(offset, count, search);
    }

    @Override
    public NeoSmartContract getNeoSmartContract(String contractIdOrName) {
        return getNeoSmartContractDao().getNeoSmartContract(contractIdOrName);
    }

    @Override
    public NeoSmartContract patchNeoSmartContract(PatchNeoSmartContractRequest patchNeoSmartContractRequest) {
        return getNeoSmartContractDao().patchNeoSmartContract(patchNeoSmartContractRequest);
    }

    @Override
    public NeoSendRawTransaction mintToken(MintTokenRequest mintTokenRequest) {
        NeoWallet neoWallet = getNeoWalletDao().getWallet(mintTokenRequest.getWalletId());
        NEP6Wallet nepWallet = neow3JClient.elementsWalletToNEP6(neoWallet.getWallet());
        Account account = Account.fromNEP6Account(nepWallet.getAccounts().get(0));
        for (var tid : mintTokenRequest.getTokenIds()) {
            var token = getNeoTokenDao().getToken(tid);
            NeoSmartContract contract = getNeoSmartContractDao().getNeoSmartContract(mintTokenRequest.getContractId());

            switch(contract.getBlockchain()){
                case "NEO":

                    try {
                        SmartContract smartContract = getNeow3JClient().getSmartContract(contract.getScriptHash());
                        var tokenIdParam = ContractParameter.string(tid);
                        var tokenMapParam = getNeow3JClient().convertObject(getObjectMapper().convertValue(token.getToken(), Map.class));
//                        List<ContractParameter> params = Arrays.asList(tokenIdParam, tokenMapParam);
//                        NeoInvokeFunction testresponse = smartContract.callInvokeFunction("mintMap", params, AccountSigner.calledByEntry(account));

                        NeoSendRawTransaction response = smartContract.invokeFunction("mintMap", tokenIdParam, tokenMapParam)
                                .signers(AccountSigner.calledByEntry(account))
                                .sign()
                                .send();

                        return response;

                    } catch (Throwable e){

                    }
                    break;
            }
        }
        return null;
    }

    @Override
    public NeoSendRawTransaction invoke(InvokeContractRequest invokeRequest, String method, List<String> params) {
        NeoSmartContract contract = getNeoSmartContractDao().getNeoSmartContract(invokeRequest.getContractId());

        switch(contract.getBlockchain()){
            case "NEO":
                SmartContract smartContract = getNeow3JClient().getSmartContract(contract.getScriptHash());
                NeoWallet neoWallet = getNeoWalletDao().getWallet(invokeRequest.getWalletId());
                NEP6Wallet nepWallet = neow3JClient.elementsWalletToNEP6(neoWallet.getWallet());
                Account account = Account.fromNEP6Account(nepWallet.getAccounts().get(0));
                if (params.size() > 0){
                    //TODO invoke method passing params
                } else {
                    try {
                        NeoInvokeFunction response = smartContract.callInvokeFunction(method, AccountSigner.calledByEntry(account));
//                        NeoSendRawTransaction response = smartContract.invokeFunction("mint", tokenIdParam, tokenParam)
//                                .signers(AccountSigner.calledByEntry(account))
//                                .sign()
//                                .send();
                    } catch (Throwable e){

                    }
                }
                break;
        }
        return null;
    }

    @Override
    public NeoInvokeFunction testInvoke(InvokeContractRequest invokeRequest, String method, List<String> params) {
        NeoSmartContract contract = getNeoSmartContractDao().getNeoSmartContract(invokeRequest.getContractId());

        switch(contract.getBlockchain()){
            case "NEO":
                SmartContract smartContract = getNeow3JClient().getSmartContract(contract.getScriptHash());
                NeoWallet neoWallet = getNeoWalletDao().getWallet(invokeRequest.getWalletId());
                NEP6Wallet nepWallet = neow3JClient.elementsWalletToNEP6(neoWallet.getWallet());
                Account account = Account.fromNEP6Account(nepWallet.getAccounts().get(0));
                if (params.size() > 0){
                    //TODO invoke method passing params
                } else {
                    try {
                        NeoInvokeFunction response = smartContract.callInvokeFunction(method, AccountSigner.calledByEntry(account));
                        return response;
                    } catch (Throwable e){

                    }
                }
                break;
        }
        return null;
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
