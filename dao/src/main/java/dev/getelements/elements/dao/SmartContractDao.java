package dev.getelements.elements.dao;

import dev.getelements.elements.exception.blockchain.SmartContractNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.BlockchainApi;
import dev.getelements.elements.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.model.blockchain.contract.SmartContract;

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
     * Gets the smart contracts in the database.
     *
     * @param offset the offset
     * @param count the count
     * @param blockchainApi the blockchain API, or null
     * @param blockchainNetwork the network associated, or null
     * @return a {@link Pagination<SmartContract>}
     */
    default Pagination<SmartContract> getSmartContractsForSingleNetwork(
            int offset, int count,
            BlockchainApi blockchainApi, BlockchainNetwork blockchainNetwork) {
        return blockchainNetwork == null ?
                getSmartContracts(offset, count, blockchainApi, null) :
                getSmartContracts(offset, count, blockchainApi, List.of(blockchainNetwork));
    }


    /**
     * Gets the specific smart contract.
     * @param contractId the contract id
     *
     * @return the contract, never null
     */
    default SmartContract getSmartContractById(final String contractId) {
        return findSmartContractById(contractId).orElseThrow(SmartContractNotFoundException::new);
    }

    /**
     * Finds a specific {@link SmartContract}.
     *
     * @param contractId
     * @return the {@link Optional<SmartContract>}
     */
    Optional<SmartContract> findSmartContractById(String contractId);

    /**
     * Gets the specific smart contract.
     *
     * @param contractNameOrId the contract unique name or id
     *
     * @return the contract, never null
     */
    default SmartContract getSmartContractByNameOrId(final String contractNameOrId) {
        return findSmartContractByNameOrId(contractNameOrId).orElseThrow(SmartContractNotFoundException::new);
    }

    /**
     * Finds a specific {@link SmartContract}.
     *
     * @param contractNameOrId the contract unique name or id
     *
     * @return the {@link Optional<SmartContract>}
     */
    Optional<SmartContract> findSmartContractByNameOrId(String contractNameOrId);

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
