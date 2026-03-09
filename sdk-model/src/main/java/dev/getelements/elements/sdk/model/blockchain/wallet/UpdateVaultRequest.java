package dev.getelements.elements.sdk.model.blockchain.wallet;



import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/** Represents a request to update a vault's properties and optionally change its passphrase. */
public class UpdateVaultRequest {

    /** Creates a new instance. */
    public UpdateVaultRequest() {}

    @NotNull
    @Schema(description =
            "A user-defined name for the vault. This is used simply for the user's reference and has no bearing  on" +
                    "the vault's functionality.")
    private String displayName;

    @NotNull
    @Schema(description = "The elements-defined user ID to own the vault.")
    private String userId;

    @Schema(description =
            "The current passphrase for the vault. If left null, no updates to the passphrase will be made. " +
                    "If not-null, then the new password must also not be null.")
    private String passphrase;

    @Schema(description =
            "The updated passphrase for the vault. If left null, no updates to the passphrase will be made. " +
                    "If not-null, then the password must also not be null.")
    private String newPassphrase;

    /**
     * Returns the display name of the vault.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of the vault.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the user ID that owns the vault.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID that owns the vault.
     *
     * @param userId the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the current passphrase for the vault.
     *
     * @return the current passphrase
     */
    public String getPassphrase() {
        return passphrase;
    }

    /**
     * Sets the current passphrase for the vault.
     *
     * @param passphrase the current passphrase
     */
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    /**
     * Returns the new passphrase for the vault.
     *
     * @return the new passphrase
     */
    public String getNewPassphrase() {
        return newPassphrase;
    }

    /**
     * Sets the new passphrase for the vault.
     *
     * @param newPassphrase the new passphrase
     */
    public void setNewPassphrase(String newPassphrase) {
        this.newPassphrase = newPassphrase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateVaultRequest that = (UpdateVaultRequest) o;
        return Objects.equals(getDisplayName(), that.getDisplayName()) && Objects.equals(getUserId(), that.getUserId()) && Objects.equals(getPassphrase(), that.getPassphrase()) && Objects.equals(getNewPassphrase(), that.getNewPassphrase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDisplayName(), getUserId(), getPassphrase(), getNewPassphrase());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpdateVaultRequest{");
        sb.append("displayName='").append(displayName).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", passphrase='").append(passphrase).append('\'');
        sb.append(", newPassphrase='").append(newPassphrase).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
