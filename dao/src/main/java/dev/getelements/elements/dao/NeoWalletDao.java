package dev.getelements.elements.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.neo.Nep6Wallet;
import dev.getelements.elements.model.blockchain.neo.UpdateNeoWalletRequest;
import dev.getelements.elements.model.blockchain.neo.NeoWallet;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Created by garrettmcspadden on 11/12/21.
 */
@Expose({
        @ModuleDefinition("eci.elements.dao.neo.wallet"),
        @ModuleDefinition(
                value = "namazu.elements.dao.neo.wallet",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.neo.wallet instead")
        ),
        @ModuleDefinition(
                value = "namazu.socialengine.dao.neo.wallet",
                deprecated = @DeprecationDefinition("Use eci.elements.dao.neo.wallet instead")
        )
})
public interface NeoWalletDao {

    /**
     * Lists all {@link NeoWallet} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param userId
     * @return a {@link Pagination} of {@link NeoWallet} instances
     */
    Pagination<NeoWallet> getWallets(int offset, int count, String userId);

    /**
     * Fetches a specific {@link NeoWallet} instance based on ID. If not found, an
     * exception is raised.
     *
     * @param walletNameOrId the wallet name or ID to
     * @return the {@link NeoWallet}, never null
     */
    NeoWallet getWallet(String walletNameOrId);

    /**
     * Tries to fetch a users specific {@link NeoWallet} instance based on name. Returns null if specified named wallet is not found.
     *
     * @param userId the user ID to check for the wallet
     * @param walletName the wallet name
     * @return the {@link NeoWallet}
     */
    NeoWallet getWalletForUser(String userId, String walletName);

    /**
     * Updates the supplied {@link NeoWallet}.
     *
     * @param walletId the id of the wallet to update
     * @param updatedWalletRequest the {@link UpdateNeoWalletRequest} with the information to update
     * @param updatedWallet the {@link Nep6Wallet} with the updated information
     * @return the {@link NeoWallet} as it was changed by the service.
     */
    NeoWallet updateWallet(String walletId, UpdateNeoWalletRequest updatedWalletRequest, Nep6Wallet updatedWallet) throws JsonProcessingException;

    /**
     * Creates a new Wallet.
     *
     * @param wallet the {@link NeoWallet} with the information to create
     * @return the {@link NeoWallet} as it was created by the service.
     */
    NeoWallet createWallet(NeoWallet wallet) throws JsonProcessingException;

    /**
     * Deletes the {@link NeoWallet} with the supplied wallet ID.
     *
     * @param walletId the template ID.
     */
    void deleteWallet(String walletId);

}
