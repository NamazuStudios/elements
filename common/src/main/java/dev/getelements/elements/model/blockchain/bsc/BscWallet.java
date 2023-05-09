package dev.getelements.elements.model.blockchain.bsc;

import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Deprecated
@ApiModel(description = "BSC Blockchain Wallet. Deprecated. Use Wallet Instead.")
public class BscWallet {

    @Deprecated
    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The unique ID of the wallet itself.")
    private String id;

    @NotNull
    @ApiModelProperty("The User associated with this wallet.")
    private User user;

    @NotNull
    @ApiModelProperty("The name given to this wallet.")
    private String displayName;

    @Valid
    @NotNull
    @Deprecated
    @ApiModelProperty(value = "The Web3j wallet file.", hidden = true)
    private Web3jWallet wallet;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Web3jWallet getWallet() {
        return wallet;
    }

    public void setWallet(Web3jWallet wallet) {
        this.wallet = wallet;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
