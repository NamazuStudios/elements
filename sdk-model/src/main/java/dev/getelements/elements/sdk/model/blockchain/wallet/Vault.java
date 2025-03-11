package dev.getelements.elements.sdk.model.blockchain.wallet;

import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Read;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.Objects;

public class Vault {

    @Null(groups = {Insert.class})
    @NotNull(groups = {Update.class, Read.class})
    @Schema(description = "The system assigned unique id of the vault.")
    private String id;

    @NotNull
    @Schema(description = "The User associated with this vault.")
    private User user;

    @NotNull
    @Schema(description = "The display name given to this vault.")
    private String displayName;

    @Valid
    @NotNull
    @Schema(description = "The Vault's key. The vault secures each vault with this key.")
    private VaultKey key;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public VaultKey getKey() {
        return key;
    }

    public void setKey(VaultKey key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vault vault = (Vault) o;
        return Objects.equals(getId(), vault.getId()) && Objects.equals(getUser(), vault.getUser()) && Objects.equals(getDisplayName(), vault.getDisplayName()) && Objects.equals(getKey(), vault.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUser(), getDisplayName(), getKey());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Vault{");
        sb.append("id='").append(id).append('\'');
        sb.append(", user=").append(user);
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", key=").append(key);
        sb.append('}');
        return sb.toString();
    }

}
