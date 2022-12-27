package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.SmartContractDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.CreateSmartContractRequest;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;
import com.namazustudios.socialengine.model.blockchain.contract.UpdateSmartContractRequest;
import com.namazustudios.socialengine.util.ValidationHelper;

import javax.inject.Inject;
import java.util.List;

public class SuperUserSmartContractService  implements SmartContractService {

    private SmartContractDao smartContractDao;

    private ValidationHelper validationHelper;

    @Override
    public Pagination<SmartContract> getSmartContracts(
            final int offset,
            final int count,
            final BlockchainApi blockchainApi,
            final List<BlockchainNetwork> blockchainNetworks) {
        return getSmartContractDao().getSmartContracts(offset, count, blockchainApi, blockchainNetworks);
    }

    @Override
    public SmartContract getSmartContract(final String contractId) {
        return getSmartContractDao().getSmartContract(contractId);
    }

    @Override
    public SmartContract createSmartContract(final CreateSmartContractRequest createSmartContractRequest) {

        getValidationHelper().validateModel(createSmartContractRequest);

        final var smartContract = new SmartContract();

        final var api = createSmartContractRequest.getApi();
        final var address = createSmartContractRequest.getAddress();
        final var displayName = createSmartContractRequest.getDisplayName();
        final var walletId = createSmartContractRequest.getWalletId();
        final var metadata = createSmartContractRequest.getMetadata();

        final var networks = createSmartContractRequest.getNetworks();
        smartContract.getApi().validate(networks);

        smartContract.setApi(api);
        smartContract.setAddress(address);
        smartContract.setNetworks(networks);
        smartContract.setMetadata(metadata);
        smartContract.setWalletId(walletId);
        smartContract.setDisplayName(displayName);

        return getSmartContractDao().createSmartContract(smartContract);

    }

    @Override
    public SmartContract updateSmartContract(
            final String smartContractId,
            final UpdateSmartContractRequest updateSmartContractRequest) {

        getValidationHelper().validateModel(updateSmartContractRequest);

        final var smartContract = getSmartContractDao().getSmartContract(smartContractId);

        final var address = updateSmartContractRequest.getAddress();
        final var displayName = updateSmartContractRequest.getDisplayName();
        final var walletId = updateSmartContractRequest.getWalletId();
        final var metadata = updateSmartContractRequest.getMetadata();

        final var networks = updateSmartContractRequest.getNetworks();
        smartContract.getApi().validate(networks);

        smartContract.setAddress(address);
        smartContract.setNetworks(networks);
        smartContract.setMetadata(metadata);
        smartContract.setWalletId(walletId);
        smartContract.setDisplayName(displayName);

        return getSmartContractDao().updateSmartContract(smartContract);


    }

    @Override
    public void deleteContract(final String contractId) {
        getSmartContractDao().deleteContract(contractId);
    }

    public SmartContractDao getSmartContractDao() {
        return smartContractDao;
    }

    @Inject
    public void setSmartContractDao(SmartContractDao smartContractDao) {
        this.smartContractDao = smartContractDao;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
