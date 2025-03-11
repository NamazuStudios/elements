package dev.getelements.elements.sdk.model.blockchain.wallet;



import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class UpdateVaultRequest {

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getNewPassphrase() {
        return newPassphrase;
    }

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
