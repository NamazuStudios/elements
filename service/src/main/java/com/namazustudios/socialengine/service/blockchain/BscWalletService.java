package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.Token;
import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscWalletRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscWalletRequest;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;

import java.util.List;

/**
 * Manages instances of {@link BscWallet}.
 *
 * Created by keithhudnall on 9/22/21.
 */
@Expose({
        @ExposedModuleDefinition(value = "namazu.elements.service.blockchain.wallet"),
        @ExposedModuleDefinition(
                value = "namazu.elements.service.blockchain.unscoped.wallet",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )
})
public interface BscWalletService {

    /**
     * Lists all {@link BscWallet} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param userId the user ID to fetch wallets for.
     * @return a {@link Pagination} of {@link BscWallet} instances
     */
    Pagination<BscWallet> getWallets(int offset, int count, String userId);

    /**
     * Fetches a specific {@link BscWallet} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param walletNameOrId the wallet Id or name
     * @return the {@link BscWallet}, never null
     */
    BscWallet getWallet(String walletNameOrId);

    /**
     * Updates the supplied {@link BscWallet}.
     *
     * @param walletId the Id of the wallet to update.
     * @param walletRequest the {@link UpdateBscWalletRequest} with the information to update
     * @return the {@link BscWallet} as it was changed by the service.
     */
    BscWallet updateWallet(String walletId, UpdateBscWalletRequest walletRequest);

    /**
     * Creates a new Wallet.
     *
     * @param walletRequest the {@link CreateBscWalletRequest} with the information to create
     * @return the {@link BscWallet} as it was created by the service.
     */
    BscWallet createWallet(CreateBscWalletRequest walletRequest);

    /**
     * Deletes the {@link BscWallet} with the supplied wallet ID.
     *
     * @param walletId the wallet Id.
     */
    void deleteWallet(String walletId);
}
