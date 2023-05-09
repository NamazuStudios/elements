package dev.getelements.elements.service;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.wallet.CreateVaultRequest;
import dev.getelements.elements.model.blockchain.wallet.CreateWalletRequest;
import dev.getelements.elements.model.blockchain.wallet.UpdateVaultRequest;
import dev.getelements.elements.model.blockchain.wallet.Vault;
import dev.getelements.elements.model.blockchain.wallet.Wallet;
import dev.getelements.elements.model.crypto.PrivateKeyCrytpoAlgorithm;

import static dev.getelements.elements.model.crypto.PrivateKeyCrytpoAlgorithm.RSA_512;

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
