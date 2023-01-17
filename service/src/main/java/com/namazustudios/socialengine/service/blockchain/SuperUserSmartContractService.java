package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.SmartContractDao;
import com.namazustudios.socialengine.dao.VaultDao;
import com.namazustudios.socialengine.exception.InvalidDataException;
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

    private VaultDao vaultDao;

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

        final var name = createSmartContractRequest.getName();
        final var addresses = createSmartContractRequest.getAddresses();
        final var displayName = createSmartContractRequest.getDisplayName();
        final var metadata = createSmartContractRequest.getMetadata();

        final var vault = getVaultDao()
                .findVault(createSmartContractRequest.getVaultId())
                .orElseThrow(InvalidDataException::new);

        final var smartContract = new SmartContract();
        smartContract.setName(name);
        smartContract.setVault(vault);
        smartContract.setAddresses(addresses);
        smartContract.setMetadata(metadata);
        smartContract.setDisplayName(displayName);
        smartContract.setName(createSmartContractRequest.getName());

        return getSmartContractDao().createSmartContract(smartContract);

    }

    @Override
    public SmartContract updateSmartContract(
            final String smartContractId,
            final UpdateSmartContractRequest updateSmartContractRequest) {

        getValidationHelper().validateModel(updateSmartContractRequest);

        final var smartContract = getSmartContractDao().getSmartContract(smartContractId);

        final var name = updateSmartContractRequest.getName();
        final var addresses = updateSmartContractRequest.getAddresses();
        final var displayName = updateSmartContractRequest.getDisplayName();
        final var metadata = updateSmartContractRequest.getMetadata();
        final var vault = getVaultDao()
                .findVault(updateSmartContractRequest.getVaultId())
                .orElseThrow(InvalidDataException::new);

        smartContract.setName(name);
        smartContract.setVault(vault);
        smartContract.setAddresses(addresses);
        smartContract.setMetadata(metadata);
        smartContract.setDisplayName(displayName);

        return getSmartContractDao().updateSmartContract(smartContract);


    }

    public VaultDao getVaultDao() {
        return vaultDao;
    }

    @Inject
    public void setVaultDao(VaultDao vaultDao) {
        this.vaultDao = vaultDao;
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
