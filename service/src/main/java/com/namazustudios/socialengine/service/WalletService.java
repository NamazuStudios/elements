package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainProtocol;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscWalletRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.UpdateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

import java.util.List;

/**
 * Manages blockchain wallets.
 */
@Expose({
        @ModuleDefinition(
                value = "namazu.elements.service.blockchain.wallet"),
        @ModuleDefinition(
                value = "namazu.elements.service.blockchain.unscoped.wallet",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )
})
public interface WalletService {

    /**
     * Lists all {@link BscWallet} instances, specifying a search query.
     *
     * @param offset the offset
     * @param count the count
     * @param userId the userId, or null
     * @param protocol the protocol, or null
     * @param network the network, or null
     * @return a {@link Pagination} of {@link BscWallet} instances
     */
    Pagination<Wallet> getWallets(
            int offset, int count,
            String userId, BlockchainProtocol protocol, List<BlockchainNetwork> networks);

    /**
     * Fetches a specific {@link BscWallet} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param walletNameOrId the wallet Id or name
     * @return the {@link BscWallet}, never null
     */
    Wallet getWallet(String walletNameOrId);

    /**
     * Updates the supplied {@link BscWallet}.
     *
     * @param walletId the Id of the wallet to update.
     * @param walletRequest the {@link UpdateBscWalletRequest} with the information to update
     * @return the {@link BscWallet} as it was changed by the service.
     */
    Wallet updateWallet(String walletId, UpdateWalletRequest walletRequest);

    /**
     * Creates a new Wallet.
     *
     * @param walletRequest the {@link CreateBscWalletRequest} with the information to create
     * @return the {@link BscWallet} as it was created by the service.
     */
    Wallet createWallet(CreateWalletRequest walletRequest);

    /**
     * Deletes the {@link BscWallet} with the supplied wallet ID.
     *
     * @param walletId the wallet Id.
     */
    void deleteWallet(String walletId);

}
