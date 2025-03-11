package dev.getelements.elements.sdk.service.blockchain;

import dev.getelements.elements.rt.annotation.RemotelyInvokable;
import dev.getelements.elements.rt.annotation.Serialize;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.blockchain.BlockchainApi;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.sdk.model.blockchain.wallet.CreateWalletRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.UpdateWalletRequest;
import dev.getelements.elements.sdk.model.blockchain.wallet.Wallet;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Manages blockchain wallets.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface WalletService {

    /**
     * Lists all {@link Wallet} instances, specifying a search query.
     *
     * @param offset the offset
     * @param count the count
     * @param vaultId
     * @param userId the userId, or null
     * @param protocol the protocol, or null
     * @return a {@link Pagination} of {@link Wallet} instances
     */
    @RemotelyInvokable
    Pagination<Wallet> getWallets(
            @Serialize("offset") int offset,
            @Serialize("count") int count,
            @Serialize("vaultId") String vaultId, @Serialize("userId") String userId,
            @Serialize("protocol") BlockchainApi protocol,
            @Serialize("networks") List<BlockchainNetwork> networks);

    /**
     * Fetches a specific {@link Wallet} instance based on ID or name.  If not found, an
     * exception is raised.
     *
     * @param walletId the wallet Id or name
     * @return the {@link Wallet}, never null
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
    Wallet getWalletInVault(@Serialize("walletId") String walletId,
                            @Serialize("vaultId") String vaultId);

    /**
     * Updates the supplied {@link Wallet}.
     *
     *
     * @param vaultId
     * @param walletId the Id of the wallet to update.
     * @param walletUpdateRequest the {@link UpdateWalletRequest} with the information to update
     * @return the {@link Wallet} as it was changed by the service.
     */
    @RemotelyInvokable
    Wallet updateWallet(
            String vaultId, @Serialize("walletId") String walletId,
            @Serialize("updateRequest") UpdateWalletRequest walletUpdateRequest);

    /**
     * Creates a new Wallet.
     *
     * @param createWalletRequest the {@link CreateWalletRequest} with the information to create
     * @return the {@link Wallet} as it was created by the service.
     */
    @RemotelyInvokable
    Wallet createWallet(
            @Serialize("vaultId") String vaultId,
            @Serialize("createWalletRequest") CreateWalletRequest createWalletRequest);

    /**
     * Deletes the {@link Wallet} with the supplied wallet ID.
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
    void deleteWalletFromVault(
            @Serialize("walletId") String walletId,
            @Serialize("vaultId") String vaultId);

}
