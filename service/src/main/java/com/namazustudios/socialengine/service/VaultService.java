package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateVaultRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.CreateWalletRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.UpdateVaultRequest;
import com.namazustudios.socialengine.model.blockchain.wallet.Vault;
import com.namazustudios.socialengine.model.blockchain.wallet.Wallet;
import com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm;

import static com.namazustudios.socialengine.model.crypto.PrivateKeyCrytpoAlgorithm.RSA_512;

/**
 * Manages instances of {@link Vault}s
 */
public interface VaultService {

    /**
     * The default vault storage algorithm.
     */
    PrivateKeyCrytpoAlgorithm DEFAULT_VAULT_ALGORITHM = RSA_512;

    /**
     * Gets all vaults, optionally filtered by user ID
     * @param offset the offset
     * @param count the count
     * @param userId the user id
     * @return a {@link Pagination<Vault>}
     */
    Pagination<Vault> getVaults(int offset, int count, String userId);

    /**
     * Gets a specific vault wiht the supplied id.
     *
     * @param vaultId the vault's id
     * @return the {@link Vault}
     */
    Vault getVault(String vaultId);

    /**
     * Creates a new {@link Vault}.
     *
     * @param request the {@link CreateWalletRequest}
     * @return the {@link Vault} as was written to the database
     */
    Vault createVault(CreateVaultRequest request);

    /**
     * Updates an existing {@link Vault}.
     *
     * @param request the {@link CreateWalletRequest}
     * @return the {@link Vault} as was written to the database
     */
    Vault updateVault(String vaultId, UpdateVaultRequest request);

    /**
     * Deletes a {@link Vault}.
     *
     * @param vaultId the vault's id
     */
    void deleteVault(String vaultId);

}
