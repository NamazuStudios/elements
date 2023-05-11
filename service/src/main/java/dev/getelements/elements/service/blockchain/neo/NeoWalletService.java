package dev.getelements.elements.service.blockchain.neo;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.Token;
import dev.getelements.elements.model.blockchain.neo.CreateNeoWalletRequest;
import dev.getelements.elements.model.blockchain.neo.NeoWallet;
import dev.getelements.elements.model.blockchain.neo.UpdateNeoWalletRequest;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import dev.getelements.elements.service.Unscoped;

import java.util.List;

/**
 * Manages instances of {@link NeoWallet}.
 *
 * Created by keithhudnall on 9/22/21.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.blockchain.neo.wallet"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.blockchain.unscoped.neo.wallet",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.blockchain.neo.wallet",
                deprecated = @DeprecationDefinition("Use eci.elements.service.blockchain.neo.wallet instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.blockchain.unscoped.neo.wallet",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use eci.elements.service.blockchain.unscoped.neo.wallet instead.")
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
     * @param walletNameOrId the wallet Id or name
     * @return the {@link NeoWallet}, never null
     */
    NeoWallet getWallet(String walletNameOrId);

    /**
     * Updates the supplied {@link NeoWallet}.
     *
     * @param walletId the Id of the wallet to update.
     * @param walletRequest the {@link UpdateNeoWalletRequest} with the information to update
     * @return the {@link NeoWallet} as it was changed by the service.
     */
    NeoWallet updateWallet(String walletId, UpdateNeoWalletRequest walletRequest);

    /**
     * Creates a new Wallet.
     *
     * @param walletRequest the {@link CreateNeoWalletRequest} with the information to create
     * @return the {@link NeoWallet} as it was created by the service.
     */
    NeoWallet createWallet(CreateNeoWalletRequest walletRequest);

    /**
     * Fetches a specific {@link NeoWallet} instance based on ID or name and then the NFT contents therein.
     * If not found, an exception is raised.
     *
     * @param walletNameOrId the wallet Id or name
     * @return the {@link NeoWallet}, never null
     */
    List<Token> getWalletNFTContents(String walletNameOrId);

    /**
     * Deletes the {@link NeoWallet} with the supplied wallet ID.
     *
     * @param walletId the wallet Id.
     */
    void deleteWallet(String walletId);
}
