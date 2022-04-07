package com.namazustudios.socialengine.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.Web3jWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscWalletRequest;
import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Created by garrettmcspadden on 11/12/21.
 */
@Expose({
        @ExposedModuleDefinition("namazu.elements.dao.neowallet"),
        @ExposedModuleDefinition(
                value = "namazu.socialengine.dao.neowallet",
                deprecated = @DeprecationDefinition("Use namazu.elements.dao.neowallet instead"))
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
     * Tries to fetch a users specific {@link BscWallet} instance based on name. Returns null if specified named wallet is not found.
     *
     * @param userId the user ID to check for the wallet
     * @param walletName the wallet name
     * @return the {@link BscWallet}
     */
    BscWallet getWalletForUser(String userId, String walletName);

    /**
     * Updates the supplied {@link BscWallet}.
     *
     * @param walletId the id of the wallet to update
     * @param updatedWalletRequest the {@link UpdateBscWalletRequest} with the information to update
     * @param updatedWallet the {@link Web3jWallet} with the updated information
     * @return the {@link BscWallet} as it was changed by the service.
     */
    BscWallet updateWallet(String walletId, UpdateBscWalletRequest updatedWalletRequest, Web3jWallet updatedWallet) throws JsonProcessingException;

    /**
     * Creates a new Wallet.
     *
     * @param wallet the {@link BscWallet} with the information to create
     * @return the {@link BscWallet} as it was created by the service.
     */
    BscWallet createWallet(BscWallet wallet) throws JsonProcessingException;

    /**
     * Deletes the {@link BscWallet} with the supplied wallet ID.
     *
     * @param walletId the template ID.
     */
    void deleteWallet(String walletId);
}
