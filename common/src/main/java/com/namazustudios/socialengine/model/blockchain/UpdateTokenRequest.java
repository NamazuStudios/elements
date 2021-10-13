package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(description = "Represents a request to create a Smart Contract Template for an Application.")
public class UpdateTokenRequest {

    @ApiModelProperty("The id of the deployed token to update.")
    @NotNull
    private String tokenId;

    @ApiModelProperty("The id of the smart contract template to deploy.")
    @NotNull
    private String templateId;

    @ApiModelProperty("The name of this token.")
    @NotNull
    private String name;

    @ApiModelProperty("The description of this token.")
    public String description;

    @NotNull
    @ApiModelProperty("The type of this token. Valid options are " +
            "\"purchase\" : ownership is transferred to the purchaser, " +
            "\"license\" : the minter of the token retains ownership, but grants access to the purchaser, and " +
            "\"rent\" : same as license, but access is revoked after a certain period of time (see rentDuration).")
    public String type;

    @NotNull
    @ApiModelProperty("Any tags to assist in filtering/searching for this token.")
    public List<String> tags;

    @ApiModelProperty("The royalty percentage to be processed on resale, if any.")
    public int royaltyPercentage;

    @ApiModelProperty("The duration of the rental before it is automatically returned (in seconds). " +
            "Only valid for rent type tokens")
    public long rentDuration;

    @ApiModelProperty("The quantity of copies of this token that can be distributed.")
    public long quantity;

    @NotNull
    @ApiModelProperty("The transfer options of this token. Valid values are " +
            "\"none\" : Cannot be transferred, " +
            "\"resale_only\" : Can be resold, but not traded, " +
            "\"trades_only\" : Can be traded, but not resold, and " +
            "\"resale_and_trades\" : Can be either resold or traded.")
    public String transferOptions;

    @NotNull
    @ApiModelProperty("Indicates whether or not this can be viewed publicly. " +
            "If false, only the previewUrl can be viewed publicly.")
    public boolean publiclyAccessible;

    @ApiModelProperty("The URL pointed at any preview of the contents of this token.")
    public String previewUrl;

    @NotNull
    @ApiModelProperty("The asset URLs of this token.")
    public List<String> assetUrls;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getRoyaltyPercentage() {
        return royaltyPercentage;
    }

    public void setRoyaltyPercentage(int royaltyPercentage) {
        this.royaltyPercentage = royaltyPercentage;
    }

    public long getRentDuration() {
        return rentDuration;
    }

    public void setRentDuration(long rentDuration) {
        this.rentDuration = rentDuration;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getTransferOptions() {
        return transferOptions;
    }

    public void setTransferOptions(String transferOptions) {
        this.transferOptions = transferOptions;
    }

    public boolean isPubliclyAccessible() {
        return publiclyAccessible;
    }

    public void setPubliclyAccessible(boolean publiclyAccessible) {
        this.publiclyAccessible = publiclyAccessible;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public List<String> getAssetUrls() {
        return assetUrls;
    }

    public void setAssetUrls(List<String> assetUrls) {
        this.assetUrls = assetUrls;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
}
