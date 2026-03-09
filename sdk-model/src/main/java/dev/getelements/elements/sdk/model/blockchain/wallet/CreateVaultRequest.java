package dev.getelements.elements.sdk.model.blockchain.wallet;

import dev.getelements.elements.sdk.model.crypto.PrivateKeyCrytpoAlgorithm;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Represents a request to create a new blockchain vault. */
public class CreateVaultRequest {

    /** Creates a new instance. */
    public CreateVaultRequest() {}

    @NotNull
    @Schema(description = "The elements-defined user ID to own the vault.")
    private String userId;

    @NotNull
    @Schema(description = 
            "A user-defined name for the vault. This is used simply for the user's reference and has no bearing  on" +
            "the vault's functionality.")
    private String displayName;

    @Schema(description =
            "The passphrase used to to encrypt the vault. If empty, then the vault will not be encrypted. Some " +
            "configurations may opt to disallow encryption entirely."
    )
    private String passphrase;

    @Schema(description = 
            "The encryption algorithm used to secure the vault. Once crated, a vault will contains a private/public " +
            "key pair which will be used to encrypt the wallets within the vault."
    )
    private PrivateKeyCrytpoAlgorithm algorithm;

    /**
     * Returns the display name for the vault.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name for the vault.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the user ID that will own the vault.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID that will own the vault.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the passphrase used to encrypt the vault.
     *
     * @return the passphrase
     */
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * Sets the passphrase used to encrypt the vault.
     *
     * @param passphrase the passphrase
     */
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    /**
     * Returns the encryption algorithm for the vault.
     *
     * @return the algorithm
     */
    public PrivateKeyCrytpoAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the encryption algorithm for the vault.
     *
     * @param algorithm the algorithm
     */
    public void setAlgorithm(PrivateKeyCrytpoAlgorithm algorithm) {
        this.algorithm = algorithm;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateVaultRequest that = (CreateVaultRequest) o;
        return Objects.equals(userId, that.userId) && Objects.equals(displayName, that.displayName) && Objects.equals(passphrase, that.passphrase) && algorithm == that.algorithm;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, displayName, passphrase, algorithm);
    }

    @Override
    public String toString() {
        return "CreateVaultRequest{" +
                "userId='" + userId + '\'' +
                ", displayName='" + displayName + '\'' +
                ", passphrase='" + passphrase + '\'' +
                ", algorithm=" + algorithm +
                '}';
    }

}
