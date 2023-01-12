package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.blockchain.SmartContractNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.contract.SmartContract;

import java.util.List;
import java.util.Optional;

/**
 * Manages smart contracts in the database.
 */
public interface SmartContractDao {

    /**
     * Gets the smart contracts in the database.
     *
     * @param offset the offset
     * @param count the count
     * @param blockchainApi the blockchain API, or null
     * @param blockchainNetworks the networks associated, or null
     * @return a {@link Pagination<SmartContract>}
     */
    Pagination<SmartContract> getSmartContracts(
            int offset, int count,
            BlockchainApi blockchainApi, List<BlockchainNetwork> blockchainNetworks);

    /**
     * Gets the specific smart contract.
     * @param contractNameOrId the contract id
     *
     * @return the contract, never null
     */
    default SmartContract getSmartContract(final String contractNameOrId) {
        return findSmartContract(contractNameOrId).orElseThrow(SmartContractNotFoundException::new);
    }

    /**
     * Finds a specific {@link SmartContract}.
     *
     * @param contractNameOrId
     * @return the {@link Optional<SmartContract>}
     */
    Optional<SmartContract> findSmartContract(String contractNameOrId);

    /**
     * Updates a {@link SmartContract} in the database.
     *
     * @param smartContract the {@link SmartContract}
     *
     * @return the {@link SmartContract}
     */
    SmartContract updateSmartContract(SmartContract smartContract);

    /**
     * Creates a {@link SmartContract} in the database.
     *
     * @param smartContract the {@link SmartContract}
     *
     * @return the {@link SmartContract}
     */
    SmartContract createSmartContract(SmartContract smartContract);

    /**
     * Deletes a {@link SmartContract}.
     *
     * @param contractId the contract ID.
     */
    void deleteContract(String contractId);

}
