package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.model.exception.blockchain.VaultNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.blockchain.wallet.Vault;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.Optional;

/**
 * Manages instance of the {@link Vault}.
 */
@ElementServiceExport
@ElementEventProducer(
        value = VaultDao.VAULT_CREATED,
        parameters = Vault.class,
        description = "Called when a vault was created."
)
@ElementEventProducer(
        value = VaultDao.VAULT_CREATED,
        parameters = {Vault.class, Transaction.class},
        description = "Called when a vault was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = VaultDao.VAULT_UPDATED,
        parameters = Vault.class,
        description = "Called when a vault was updated."
)
@ElementEventProducer(
        value = VaultDao.VAULT_UPDATED,
        parameters = {Vault.class, Transaction.class},
        description = "Called when a vault was updated. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = VaultDao.VAULT_DELETED,
        parameters = Vault.class,
        description = "Called when a vault was deleted."
)
@ElementEventProducer(
        value = VaultDao.VAULT_DELETED,
        parameters = {Vault.class, Transaction.class},
        description = "Called when a vault was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface VaultDao {

    String VAULT_CREATED = "dev.getelements.elements.sdk.model.dao.vault.created";

    String VAULT_UPDATED = "dev.getelements.elements.sdk.model.dao.vault.updated";

    String VAULT_DELETED = "dev.getelements.elements.sdk.model.dao.vault.deleted";

    /**
     * Gets all vaults in the system.
     *
     * @param offset the offset
     * @param count  the count
     * @param userId the user ID, or null
     * @return a {@link Pagination<Vault>}
     */
    Pagination<Vault> getVaults(int offset, int count, String userId);

    /**
     * Finds a {@link Vault}.
     *
     * @param vaultId the {@link Vault} id
     * @return an Optional {@link Vault}
     */
    Optional<Vault> findVault(String vaultId);

    /**
     * Gets a {@link Vault} the vault.
     *
     * @param vaultId the vault ID
     * @return the vault, never null
     */
    default Vault getVault(final String vaultId) {
        return findVault(vaultId).orElseThrow(VaultNotFoundException::new);
    }

    /**
     * Finds a {@link Vault} with the supplied id and user id.
     *
     * @param vaultId the vault ID
     * @param userId  the user ID
     * @return the vault ID
     */
    Optional<Vault> findVaultForUser(String vaultId, String userId);

    /**
     * Gets the the vault ID.
     *
     * @param vaultId the vault ID
     * @param userId  the user ID
     * @return the vault, never null.
     */
    default Vault getVaultForUser(final String vaultId, final String userId) {
        return findVaultForUser(vaultId, userId).orElseThrow(VaultNotFoundException::new);
    }

    /**
     * Creates a new {@link Vault}.
     *
     * @param vault the vault
     * @return the {@link Vault} as was written to the database
     */
    Vault createVault(Vault vault);

    /**
     * Updates the {@link Vault}.
     *
     * @param vault the vault
     * @return the {@link Vault} as was written to the database
     */
    Vault updateVault(Vault vault);

    /**
     * Updates a vault with the provided user. If the requested vault exists for the supplied user, then the specifed
     * vault will be updated.
     * <p>
     * If no vault exists for the supplied user, then no changes will be made to the database and the returned Optional
     * will be empty.
     *
     * @param vault  the vault to update
     * @param userId the user ID to search
     * @return an {@link Optional<Vault>}
     */
    Optional<Vault> findAndUpdateVaultBelongingToUser(Vault vault, String userId);

    /**
     * Deletes a {@link Vault} with the supplied identifier.
     *
     * @param vaultId the Vault ID
     */
    void deleteVault(String vaultId);

    /**
     * Deletes the {@link Vault} for the supplied urse
     *
     * @param vaultId the vauld ID
     * @param userId  the user ID
     */
    void deleteVaultForUser(String vaultId, String userId);

}
