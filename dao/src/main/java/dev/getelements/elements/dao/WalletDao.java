package dev.getelements.elements.dao;

import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.exception.blockchain.WalletNotFoundException;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.model.blockchain.BlockchainApi;
import dev.getelements.elements.model.blockchain.wallet.Wallet;

import java.util.List;
import java.util.Optional;

public interface WalletDao {

    /**
     * Lists all {@link Wallet} instances, specifying a search query.
     *
     * @param offset the offset
     * @param count the count
     * @param vaultId
     * @param userId the userId, or null
     * @param protocol the protocol, or null
     * @param networks the network, or null
     * @return a {@link Pagination} of {@link Wallet} instances
     */
    Pagination<Wallet> getWallets(
            int offset, int count,
            String vaultId, String userId, BlockchainApi protocol, List<BlockchainNetwork> networks);

    /**
     * Finds a {@link Wallet} based on wallet id.
     *
     * @param walletId the wallet id
     * @return the {@link Optional<Wallet>}
     */
    Optional<Wallet> findWallet(String walletId);

    /**
     * Finds a wallet belonging to the specified user..
     *
     * @param walletId the wallet id
     * @param userId the user id
     * @return the {@link Optional<Wallet>}
     */
    Optional<Wallet> findWalletForUser(String walletId, String userId);

    /**
     * Finds a wallet in the specified vault.
     *
     * @param walletId the wallet id
     * @param vaultId the vault id
     * @return the {@link Optional<Wallet>}
     */
    Optional<Wallet> findWalletInVault(String walletId, String vaultId);

    /**
     * Fetches a specific {@link Wallet} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param walletId the wallet id to
     * @return the {@link Wallet}, never null
     */
    default Wallet getWallet(final String walletId) {
        return findWallet(walletId).orElseThrow(WalletNotFoundException::new);
    }

    /**
     * Fetches a specific {@link Wallet} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param walletId the wallet id to
     * @param vaultId
     * @return the {@link Wallet}, never null
     */
    default Wallet getWalletInVault(final String walletId, final String vaultId) {
        return findWalletInVault(walletId, vaultId).orElseThrow(WalletNotFoundException::new);
    }

    /**
     * Fetches a specific {@link Wallet} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param walletId the wallet id to
     * @param userId the user id
     * @return the {@link Wallet}, never null
     */
    default Wallet getWalletForUser(final String walletId, final String userId) {
        return findWalletForUser(walletId, userId).orElseThrow(WalletNotFoundException::new);
    }

    /**
     * Attempts to find a single wallet for the supplied vault ID and blockchain network.
     *
     * @param vaultId the vault id
     * @param blockchainNetwork the network
     *
     * @return the wallet
     * @throws WalletNotFoundException if no wallet matches
     * @throws DuplicateException if more than one wallet matches
     */
    Wallet getSingleWalletFromVaultForNetwork(String vaultId, BlockchainNetwork blockchainNetwork);

    /**
     * Updates the supplied {@link Wallet}.
     *
     * @return the {@link Wallet} as it was changed by the service.
     * @param Wallet
     */
    Wallet updateWallet(Wallet Wallet);

    /**
     * Creates a new Wallet.
     *
     * @param wallet the {@link Wallet} with the information to create
     * @return the {@link Wallet} as it was created by the service.
     */
    Wallet createWallet(Wallet wallet);

    /**
     * Deletes the {@link Wallet} with the supplied wallet ID.
     *
     * @param walletId the template ID.
     * @
     */
    void deleteWallet(String walletId);

    /**
     * Deletes a wallet for the supplied user.
     *
     * @param walletId the wallet id
     * @param userId the user id
     */
    void deleteWalletForUser(String walletId, String userId);

    /**
     * Deletes a wallet for the supplied user and vault.
     *  @param walletId the wallet ID
     * @param vaultId the vault ID
     */
    void deleteWalletForVault(String walletId, String vaultId);

}
