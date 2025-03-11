package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.blockchain.WalletNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;
import java.util.Optional;

/**
 * Used to provide access to a specific blockchain's wallet.
 *
 * @param <WalletT>
 */
@ElementServiceExport
public interface ChainWalletDao<WalletT> {

    /**
     * Lists all {@link WalletT} instances, specifying a search query.
     *
     * @param offset   the offset
     * @param count    the count
     * @param userId   the userId, or null
     * @param networks the network, or null
     * @return a {@link Pagination} of {@link WalletT} instances
     */
    Pagination<WalletT> getWallets(
            int offset, int count,
            String userId, List<BlockchainNetwork> networks);

    /**
     * Finds a {@link WalletT} based on wallet id.
     *
     * @param walletId the wallet id
     * @return the {@link Optional}
     */
    default Optional<WalletT> findWallet(final String walletId) {
        return findWallet(walletId, null);
    }

    /**
     * Finds the wallet with the supplied wallet id and user id.
     *
     * @param walletId
     * @param userId
     * @return
     */
    Optional<WalletT> findWallet(String walletId, String userId);

    /**
     * Fetches a specific {@link WalletT} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param walletId the wallet id to
     * @return the {@link WalletT}, never null
     */
    default WalletT getWallet(final String walletId) {
        return findWallet(walletId, null).orElseThrow(WalletNotFoundException::new);
    }

    /**
     * Fetches a specific {@link WalletT} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param walletId the wallet id to
     * @return the {@link WalletT}, never null
     */
    default WalletT getWallet(final String walletId, final String userId) {
        return findWallet(walletId, userId).orElseThrow(WalletNotFoundException::new);
    }

    /**
     * Updates the supplied {@link WalletT}.
     *
     * @param Wallet
     * @return the {@link WalletT} as it was changed by the service.
     */
    WalletT updateWallet(WalletT Wallet);

    /**
     * Creates a new WalletT.
     *
     * @param wallet the {@link WalletT} with the information to create
     * @return the {@link WalletT} as it was created by the service.
     */
    WalletT createWallet(WalletT wallet);

    /**
     * Deletes the {@link WalletT} with the supplied wallet ID.
     *
     * @param walletId the template ID.
     */
    default void deleteWallet(String walletId) {
        deleteWallet(walletId, null);
    }

    /**
     * Deletes a wallet for the supplied user.
     *
     * @param walletId the wallet id
     * @param userId   the user id
     */
    void deleteWallet(String walletId, String userId);

}
