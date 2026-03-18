package dev.getelements.elements.sdk.model.blockchain;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.List;
import java.util.Map;

/** Represents a blockchain token definition with ownership, supply, and transfer attributes. */
public class Token {

    /** Creates a new instance. */
    public Token() {}

    @Schema(description = "The account address of the owner to be assigned when minting this token.")
    private String owner;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @Schema(description = "The name given to this token.")
    private String name;

    @Schema(description = "The description of this token.")
    private String description;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @Schema(description = "Any tags to assist in filtering/searching for this token.")
    private List<String> tags;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @Schema(description = "The maximum number of copies of this token that can be owned (by any number of accounts) at any one time.")
    private long totalSupply;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @Schema(description = "The maximum number of usages this nft will have if applicable.")
    private long usages;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @Schema(description = "The status of this token. Valid values are " +
        "\"public\" : Can be viewed by everyone, " +
        "\"private\" : Only the token or contract owner can view the token properties " +
        "\"preview\" : If not the token or contract owner, the asset urls cannot be viewed.")
    private String accessOption;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @Schema(description = "The URLs pointing at any preview of the contents of this token.")
    private List<String> previewUrls;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @Schema(description = "The asset URLs of this token.")
    private List<String> assetUrls;

    @Schema(description = "Defines the ownership for this token.")
    private Ownership ownership;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @Schema(description = "The transfer options of this token. Valid values are " +
            "\"none\" : Cannot be transferred, " +
            "\"resale_only\" : Can be resold, but not traded, " +
            "\"trades_only\" : Can be traded, but not resold, and " +
            "\"resale_and_trades\" : Can be either resold or traded.")
    private String transferOptions;

    @Schema(description = "Indicates whether or not the license is revocable by the owner")
    private boolean revocable;

    @Schema(description = "The expiration date of the license. Recorded in seconds since Unix epoch")
    private long expiry;

    @Schema(description = "If true, the licensee may pay a fee to extend the expiration date by the same difference " +
            "between the original expiry and the time of minting.")
    private boolean renewable;

    @Schema(description = "Any meta data for this token.")
    private Map<String, Object> metadata;

    /**
     * Returns the account address of the owner assigned when minting this token.
     *
     * @return the owner address
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the account address of the owner assigned when minting this token.
     *
     * @param owner the owner address
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Returns the name given to this token.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name given to this token.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of this token.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this token.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the access option for this token.
     *
     * @return the access option
     */
    public String getAccessOption() {
        return accessOption;
    }

    /**
     * Sets the access option for this token.
     *
     * @param accessOption the access option
     */
    public void setAccessOption(String accessOption) {
        this.accessOption = accessOption;
    }

    /**
     * Returns the tags for this token.
     *
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the tags for this token.
     *
     * @param tags the tags
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Returns the ownership definition for this token.
     *
     * @return the ownership
     */
    public Ownership getOwnership() {
        return ownership;
    }

    /**
     * Sets the ownership definition for this token.
     *
     * @param ownership the ownership
     */
    public void setOwnership(Ownership ownership) {
        this.ownership = ownership;
    }

    /**
     * Returns the maximum number of copies of this token that can be owned at any one time.
     *
     * @return the total supply
     */
    public long getTotalSupply() {
        return totalSupply;
    }

    /**
     * Sets the maximum number of copies of this token that can be owned at any one time.
     *
     * @param totalSupply the total supply
     */
    public void setTotalSupply(long totalSupply) {
        this.totalSupply = totalSupply;
    }

    /**
     * Returns the maximum number of usages this token will have.
     *
     * @return the usages
     */
    public long getUsages() {
        return usages;
    }

    /**
     * Sets the maximum number of usages this token will have.
     *
     * @param usages the usages
     */
    public void setUsages(long usages) {
        this.usages = usages;
    }

    /**
     * Returns the transfer options for this token.
     *
     * @return the transfer options
     */
    public String getTransferOptions() {
        return transferOptions;
    }

    /**
     * Sets the transfer options for this token.
     *
     * @param transferOptions the transfer options
     */
    public void setTransferOptions(String transferOptions) {
        this.transferOptions = transferOptions;
    }

    /**
     * Returns the preview URLs for the contents of this token.
     *
     * @return the preview URLs
     */
    public List<String> getPreviewUrls() {
        return previewUrls;
    }

    /**
     * Sets the preview URLs for the contents of this token.
     *
     * @param previewUrls the preview URLs
     */
    public void setPreviewUrls(List<String> previewUrls) {
        this.previewUrls = previewUrls;
    }

    /**
     * Returns the asset URLs of this token.
     *
     * @return the asset URLs
     */
    public List<String> getAssetUrls() {
        return assetUrls;
    }

    /**
     * Sets the asset URLs of this token.
     *
     * @param assetUrls the asset URLs
     */
    public void setAssetUrls(List<String> assetUrls) {
        this.assetUrls = assetUrls;
    }

    /**
     * Returns whether the license for this token is revocable by the owner.
     *
     * @return true if revocable
     */
    public boolean isRevocable() {
        return revocable;
    }

    /**
     * Sets whether the license for this token is revocable by the owner.
     *
     * @param revocable true if revocable
     */
    public void setRevocable(boolean revocable) {
        this.revocable = revocable;
    }

    /**
     * Returns the expiration date of the license in seconds since Unix epoch.
     *
     * @return the expiry
     */
    public long getExpiry() {
        return expiry;
    }

    /**
     * Sets the expiration date of the license in seconds since Unix epoch.
     *
     * @param expiry the expiry
     */
    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    /**
     * Returns whether the licensee may pay a fee to extend the expiration date.
     *
     * @return true if renewable
     */
    public boolean isRenewable() {
        return renewable;
    }

    /**
     * Sets whether the licensee may pay a fee to extend the expiration date.
     *
     * @param renewable true if renewable
     */
    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
    }

    /**
     * Returns any metadata for this token.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets any metadata for this token.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
