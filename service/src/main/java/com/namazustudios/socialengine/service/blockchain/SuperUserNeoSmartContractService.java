package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.NeoSmartContractDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.NeoSmartContract;
import com.namazustudios.socialengine.model.blockchain.PatchNeoSmartContractRequest;

import javax.inject.Inject;

public class SuperUserNeoSmartContractService implements NeoSmartContractService {

    private NeoSmartContractDao neoSmartContractDao;

    private Neow3jClient neow3JClient;

    @Override
    public Pagination<NeoSmartContract> getNeoSmartContracts(int offset, int count, String search) {
        var hash = neow3JClient.toHash160(search);
        var smartContract = neow3JClient.getSmartContract(hash);
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
    public void deleteTemplate(String contractId) {
        getNeoSmartContractDao().deleteNeoSmartContract(contractId);
    }

    public NeoSmartContractDao getNeoSmartContractDao() {
        return neoSmartContractDao;
    }

    @Inject
    public void setNeoSmartContractDao(NeoSmartContractDao neoSmartContractDao) {
        this.neoSmartContractDao = neoSmartContractDao;
    }

    public Neow3jClient getNeow3jClient(){return neow3JClient;}

    @Inject
    public void setNeow3jClient(Neow3jClient neow3JClient){this.neow3JClient = neow3JClient;}
}
