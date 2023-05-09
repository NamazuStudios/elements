package dev.getelements.elements.model.blockchain.wallet;

import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.ValidationGroups.Read;
import dev.getelements.elements.model.ValidationGroups.Update;
import dev.getelements.elements.model.user.User;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Objects;

public class Vault {

    @Null(groups = {Insert.class})
    @NotNull(groups = {Update.class, Read.class})
    @ApiModelProperty("The system assigned unique id of the vault.")
    private String id;

    @NotNull
    @ApiModelProperty("The User associated with this vault.")
    private User user;

    @NotNull
    @ApiModelProperty("The display name given to this vault.")
    private String displayName;

    @Valid
    @NotNull
    @ApiModelProperty("The Vault's key. The vault secures each vault with this key.")
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
