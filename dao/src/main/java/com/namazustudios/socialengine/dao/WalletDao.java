package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.blockchain.WalletNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;

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
    default Optional<Wallet> findWallet(final String walletId) {
        return findWallet(walletId, null);
    }

    /**
     * Finds the wallets.
     *
     * @param walletId the wallet id
     * @param vaultId the value id
     * @return the {@link Optional<Wallet>}
     */
    Optional<Wallet> findWallet(String walletId, String vaultId);

    /**
     * Fetches a specific {@link Wallet} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param walletId the wallet id to
     * @return the {@link Wallet}, never null
     */
    default Wallet getWallet(final String walletId) {
        return findWallet(walletId, null).orElseThrow(WalletNotFoundException::new);
    }

    /**
     * Fetches a specific {@link Wallet} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param walletId the wallet id to
     * @param vaultId
     * @return the {@link Wallet}, never null
     */
    default Wallet getWallet(final String walletId, final String vaultId) {
        return findWallet(walletId, vaultId).orElseThrow(WalletNotFoundException::new);
    }

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
