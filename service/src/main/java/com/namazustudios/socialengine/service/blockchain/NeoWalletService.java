package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.*;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;

import java.util.Optional;

/**
 * Manages instances of {@link NeoWallet}.
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
public interface NeoWalletService {

    /**
     * Lists all {@link NeoWallet} instances, specifying a search query.
     *
     * @param offset
     * @param count
     * @param userId the user ID to fetch wallets for.
     * @return a {@link Pagination} of {@link NeoWallet} instances
     */
    Pagination<NeoWallet> getWallets(int offset, int count, String userId);

    /**
     * Fetches a specific {@link NeoWallet} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param walletId the wallet Id
     * @return the {@link NeoWallet}, never null
     */
    NeoWallet getWallet(String walletId);

    /**
     * Updates the supplied {@link NeoWallet}.
     *
     * @param walletId the Id of the wallet to update.
     * @param walletRequest the {@link UpdateWalletRequest} with the information to update
     * @return the {@link NeoWallet} as it was changed by the service.
     */
    NeoWallet updateWallet(String walletId, UpdateWalletRequest walletRequest);

    /**
     * Creates a new Wallet.
     *
     * @param walletRequest the {@link CreateWalletRequest} with the information to create
     * @return the {@link NeoWallet} as it was created by the service.
     */
    NeoWallet createWallet(CreateWalletRequest walletRequest);

    /**
     * Deletes the {@link NeoWallet} with the supplied wallet ID.
     *
     * @param walletId the wallet Id.
     */
    void deleteWallet(String walletId);
}
