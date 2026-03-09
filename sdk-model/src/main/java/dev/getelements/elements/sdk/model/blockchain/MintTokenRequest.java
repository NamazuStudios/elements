package dev.getelements.elements.sdk.model.blockchain;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.List;

/**
 * Represents a request to mint a blockchain token.
 *
 * @deprecated use the new smart contract API instead
 */
@Deprecated
public class MintTokenRequest {

    /** Creates a new instance. */
    public MintTokenRequest() {}

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Schema(description = "The Elements Id of the token to mint.")
    private String tokenId;

    @Schema(description = "The address of the owner to assign the tokens to. If no owner has been specified, either " +
            "here or on the token model, then the address that invoked the mint function will be assigned as the owner.")
    private String ownerAddress;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Schema(description = "The elements wallet Id with funds to invoke the method. This will always use the default account of the wallet.")
    private String walletId;

    @NotNull(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class, ValidationGroups.Update.class})
    @Schema(description = "The password of the wallet with funds to mint.")
    private String password;

    /**
     * Returns the Elements ID of the token to mint.
     *
     * @return the token ID
     */
    public String getTokenId() {
        return tokenId;
    }

    /**
     * Sets the Elements ID of the token to mint.
     *
     * @param tokenId the token ID
     */
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    /**
     * Returns the owner address to assign the tokens to.
     *
     * @return the owner address
     */
    public String getOwnerAddress() {
        return ownerAddress;
    }

    /**
     * Sets the owner address to assign the tokens to.
     *
     * @param ownerAddress the owner address
     */
    public void setOwnerAddress(String ownerAddress) {
        this.ownerAddress = ownerAddress;
    }

    /**
     * Returns the elements wallet ID.
     *
     * @return the wallet ID
     */
    public String getWalletId() {
        return walletId;
    }

    /**
     * Sets the elements wallet ID.
     *
     * @param walletId the wallet ID
     */
    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    /**
     * Returns the wallet password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the wallet password.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
