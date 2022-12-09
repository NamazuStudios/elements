package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.blockchain.WalletNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainProtocol;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;

import java.util.Optional;

public interface WalletDao {

    /**
     * Lists all {@link Wallet} instances, specifying a search query.
     *
     * @param offset the offset
     * @param count the count
     * @param userId the userId, or null
     * @param protocol the protocol, or null
     * @param network the network, or null
     * @return a {@link Pagination} of {@link Wallet} instances
     */
    Pagination<Wallet> getWallets(
            int offset, int count,
            String userId, BlockchainProtocol protocol, BlockchainNetwork network);

    /**
     * Finds a {@link Wallet} based on wallet id.
     *
     * @param walletId the wallet id
     * @return the {@link Optional<Wallet>}
     */
    default Optional<Wallet> findWallet(final String walletId) {
        return findWallet(walletId, null);
    }

    Optional<Wallet> findWallet(String walletId, String userId);

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
     * @return the {@link Wallet}, never null
     */
    default Wallet getWallet(final String walletId, final String userId) {
        return findWallet(walletId, userId).orElseThrow(WalletNotFoundException::new);
    }

    /**
     * Tries to fetch a users specific {@link Wallet} instance based on name.
     *
     * @param userId the user ID to check for the wallet
     * @param walletName the wallet name
     * @return the {@link Wallet}, never null
     */
    Wallet getWalletForUser(String userId, String walletName);

    /**
     * Tries to fetch a users specific {@link Wallet} instance based on name. Returns an empty optional if not found.
     *
     * @param userId the user ID to check for the wallet
     * @param walletName the wallet name
     * @return the {@link Wallet}, never null
     */
    default Optional<Wallet> findWalletForUser(final String userId, final String walletName) {
        try {
            return Optional.of(getWalletForUser(userId, walletName));
        } catch (WalletNotFoundException ex) {
            return Optional.empty();
        }
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
    default void deleteWallet(String walletId) {
        deleteWallet(walletId, null);
    }

    /**
     * Deletes a wallet for the supplied user.
     *
     * @param walletId the wallet id
     * @param userId the user id
     */
    void deleteWallet(String walletId, String userId);

}
