package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
import com.namazustudios.socialengine.model.blockchain.BlockchainApi;
import com.namazustudios.socialengine.model.blockchain.bsc.BscWallet;
import com.namazustudios.socialengine.model.blockchain.bsc.CreateBscWalletRequest;
import com.namazustudios.socialengine.model.blockchain.bsc.UpdateBscWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.UpdateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.rt.annotation.*;

import java.util.List;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.API_SCOPE;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;

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
@RemoteService(
        value = "wallet",
        scopes = {
                @RemoteScope(scope = API_SCOPE, protocol = ELEMENTS_JSON_RPC_PROTOCOL)
        }
)
public interface WalletService {

    /**
     * Lists all {@link BscWallet} instances, specifying a search query.
     *
     * @param offset the offset
     * @param count the count
     * @param userId the userId, or null
     * @param vaultId
     * @param protocol the protocol, or null
     * @return a {@link Pagination} of {@link BscWallet} instances
     */
    @RemotelyInvokable
    Pagination<Wallet> getWallets(
            @Serialize("offset") int offset,
            @Serialize("count") int count,
            @Serialize("userId") String userId,
            @Serialize("vaultId") String vaultId,
            @Serialize("protocol") BlockchainApi protocol,
            @Serialize("networks") List<BlockchainNetwork> networks);

    /**
     * Fetches a specific {@link BscWallet} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param walletId the wallet Id or name
     * @return the {@link BscWallet}, never null
     */
    @RemotelyInvokable
    Wallet getWallet(@Serialize("walletId") String walletId);

    /**
     * Fetches wallet from the supplied vault.
     *
     * @param walletId the wallet id
     * @param vaultId the vault id
     * @return
     */
    @RemotelyInvokable
    Wallet getWallet(@Serialize("walletId") String walletId,
                     @Serialize("vaultId") String vaultId);

    /**
     * Updates the supplied {@link BscWallet}.
     *
     *
     * @param vaultId
     * @param walletId the Id of the wallet to update.
     * @param walletUpdateRequest the {@link UpdateBscWalletRequest} with the information to update
     * @return the {@link BscWallet} as it was changed by the service.
     */
    @RemotelyInvokable
    Wallet updateWallet(
            String vaultId, @Serialize("walletId") String walletId,
            @Serialize("updateRequest") UpdateWalletRequest walletUpdateRequest);

    /**
     * Creates a new Wallet.
     *
     * @param createWalletRequest the {@link CreateBscWalletRequest} with the information to create
     * @return the {@link BscWallet} as it was created by the service.
     */
    @RemotelyInvokable
    Wallet createWallet(
            @Serialize("vaultId") String vaultId,
            @Serialize("createWalletRequest") CreateWalletRequest createWalletRequest);

    /**
     * Deletes the {@link BscWallet} with the supplied wallet ID.
     *
     * @param walletId the wallet Id.
     */
    @RemotelyInvokable
    void deleteWallet(@Serialize("walletId") String walletId);

    /**
     * Deletes the {@link Wallet} with the supplied id, from the vault.
     * @param walletId the wallet id
     * @param vaultId the vault id
     */
    @RemotelyInvokable
    void deleteWallet(
            @Serialize("walletId") String walletId,
            @Serialize("vaultId") String vaultId);

}
