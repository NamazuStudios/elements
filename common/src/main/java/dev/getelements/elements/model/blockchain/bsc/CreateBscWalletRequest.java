package dev.getelements.elements.model.blockchain.bsc;

import dev.getelements.elements.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;

@Deprecated
@ApiModel(description = "Represents a request to create a bsc wallet.")
public class CreateBscWalletRequest {

    @ApiModelProperty("A user-defined name for the wallet..")
    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    private String displayName;

    @ApiModelProperty("The elements-defined user ID to own the wallet.")
    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    private String userId;

    @ApiModelProperty("Password to encrypt the wallet.")
    private String password;

    @ApiModelProperty("Private key (WIF) for existing account to import into wallet. If not specified, a new account will be created.")
    private String privateKey;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
