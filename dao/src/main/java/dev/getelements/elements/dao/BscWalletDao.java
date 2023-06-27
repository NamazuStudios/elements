package dev.getelements.elements.dao;

import dev.getelements.elements.exception.blockchain.BscWalletNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.bsc.BscWallet;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import java.util.Optional;

/**
 * Created by garrettmcspadden on 11/12/21.
 */
@Expose({
        @ModuleDefinition("eci.elements.dao.bsc.wallet"),
        @ModuleDefinition(
                value = "namazu.elements.dao.bsc.wallet",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.bsc.wallet instead")
        ),
        @ModuleDefinition(
                value = "namazu.socialengine.dao.bsc.wallet",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.bsc.wallet instead")
        )
})
public interface BscWalletDao {

    /**
     * Lists all {@link BscWallet} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param userId
     * @return a {@link Pagination} of {@link BscWallet} instances
     */
    Pagination<BscWallet> getWallets(int offset, int count, String userId);

    /**
     * Fetches a specific {@link BscWallet} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param walletNameOrId the wallet name or ID to
     * @return the {@link BscWallet}, never null
     */
    BscWallet getWallet(String walletNameOrId);

    /**
     * Tries to fetch a users specific {@link BscWallet} instance based on name.
     *
     * @param userId the user ID to check for the wallet
     * @param walletName the wallet name
     * @return the {@link BscWallet}, never null
     */
    BscWallet getWalletForUser(String userId, String walletName);

    /**
     * Tries to fetch a users specific {@link BscWallet} instance based on name. Returns an empty optional if not found.
     *
     * @param userId the user ID to check for the wallet
     * @param walletName the wallet name
     * @return the {@link BscWallet}, never null
     */
    default Optional<BscWallet> findWalletForUser(final String userId, final String walletName) {
        try {
            return Optional.of(getWalletForUser(userId, walletName));
        } catch (BscWalletNotFoundException ex) {
            return Optional.empty();
        }
    }

    /**
     * Updates the supplied {@link BscWallet}.
     *
     * @return the {@link BscWallet} as it was changed by the service.
     * @param bscWallet
     */
    BscWallet updateWallet(BscWallet bscWallet);

    /**
     * Creates a new Wallet.
     *
     * @param wallet the {@link BscWallet} with the information to create
     * @return the {@link BscWallet} as it was created by the service.
     */
    BscWallet createWallet(BscWallet wallet);

    /**
     * Deletes the {@link BscWallet} with the supplied wallet ID.
     *
     * @param walletId the template ID.
     */
    void deleteWallet(String walletId);

}
