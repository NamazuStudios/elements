package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Map;

public class Token {

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The account address of the owner to be assigned when minting this token.")
    private String owner;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The name given to this token.")
    private String name;

    @ApiModelProperty("The description of this token.")
    private String description;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class, ValidationGroups.Update.class})
    @ApiModelProperty("Any tags to assist in filtering/searching for this token.")
    private List<String> tags;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The maximum number of copies of this token that can be owned (by any number of accounts) at any one time.")
    private long totalSupply;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The maximum number of usages this nft will have if applicable.")
    private long usages;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The status of this token. Valid values are " +
            "\"public\" : Can be viewed by everyone, " +
            "\"private\" : Only the token or contract owner can view the token properties " +
            "\"preview\" : If not the token or contract owner, the asset urls cannot be viewed.")
    private String accessOption;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The URLs pointing at any preview of the contents of this token.")
    private List<String> previewUrls;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The asset URLs of this token.")
    private List<String> assetUrls;

    @ApiModelProperty("Defines the ownership for this token.")
    private Ownership ownership;

    @NotNull(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @Null(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The transfer options of this token. Valid values are " +
            "\"none\" : Cannot be transferred, " +
            "\"resale_only\" : Can be resold, but not traded, " +
            "\"trades_only\" : Can be traded, but not resold, and " +
            "\"resale_and_trades\" : Can be either resold or traded.")
    private String transferOptions;

    @ApiModelProperty("Indicates whether or not the license is revocable by the owner")
    private boolean revocable;

    @ApiModelProperty("The expiration date of the license. Recorded in seconds since Unix epoch")
    private long expiry;

    @ApiModelProperty("If true, the licensee may pay a fee to extend the expiration date by the same difference " +
            "between the original expiry and the time of minting.")
    private boolean renewable;

    @ApiModelProperty("Any meta data for this token.")
    private Map<String, Object> metadata;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccessOption() {
        return accessOption;
    }

    public void setAccessOption(String accessOption) {
        this.accessOption = accessOption;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Ownership getOwnership() {
        return ownership;
    }

    public void setOwnership(Ownership ownership) {
        this.ownership = ownership;
    }

    public long getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(long totalSupply) {
        this.totalSupply = totalSupply;
    }

    public long getUsages() {
        return usages;
    }

    public void setUsages(long usages) {
        this.usages = usages;
    }

    public String getTransferOptions() {
        return transferOptions;
    }

    public void setTransferOptions(String transferOptions) {
        this.transferOptions = transferOptions;
    }

    public List<String> getPreviewUrls() {
        return previewUrls;
    }

    public void setPreviewUrls(List<String> previewUrls) {
        this.previewUrls = previewUrls;
    }

    public List<String> getAssetUrls() {
        return assetUrls;
    }

    public void setAssetUrls(List<String> assetUrls) {
        this.assetUrls = assetUrls;
    }

    public boolean isRevocable() {
        return revocable;
    }

    public void setRevocable(boolean revocable) {
        this.revocable = revocable;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    public boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
