package com.namazustudios.socialengine.model.blockchain;

import com.namazustudios.socialengine.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Map;

@ApiModel
public class NeoToken {

    @NotNull(groups = ValidationGroups.Update.class)
    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @ApiModelProperty("The unique ID of the token itself.")
    private String id;

    @NotNull
    @ApiModelProperty("The name given to this token.")
    private String name;

    @ApiModelProperty("The description of this token.")
    private String description;

    @NotNull
    @ApiModelProperty("The type of this token. Valid options are " +
            "\"purchase\" : ownership is transferred to the purchaser, " +
            "\"license\" : the minter of the token retains ownership, but grants access to the purchaser, and " +
            "\"rent\" : same as license, but access is revoked after a certain period of time (see rentDuration).")
    private String type;

    @NotNull
    @ApiModelProperty("Any tags to assist in filtering/searching for this token.")
    private List<String> tags;

    @ApiModelProperty("Defines the way royalties are handled for this token.")
    private Royalties royalties;

    @ApiModelProperty("The quantity of copies of this token that can be distributed.")
    private long totalQuantity;

    @ApiModelProperty("Will this token exist as a series 1-x.")
    private boolean series;

    @ApiModelProperty("If designated as “series” in Elements, single NFTs are minted sequentially on demand up " +
            "to the total quantity, and are numbered in sequence as minted.")
    private long numberInSeries;

    @NotNull
    @ApiModelProperty("The transfer options of this token. Valid values are " +
            "\"none\" : Cannot be transferred, " +
            "\"resale_only\" : Can be resold, but not traded, " +
            "\"trades_only\" : Can be traded, but not resold, and " +
            "\"resale_and_trades\" : Can be either resold or traded.")
    private String transferOptions;

    @NotNull
    @ApiModelProperty("The status of this token. Valid values are " +
            "\"public\" : Can be viewed by everyone, " +
            "\"private\" : Only the token or contract owner can view the token properties " +
            "\"preview\" : If not the token or contract owner, the asset urls cannot be viewed.")
    private String status;

    @ApiModelProperty("The URLs pointing at any preview of the contents of this token.")
    private List<String> previewUrls;

    @NotNull
    @ApiModelProperty("The asset URLs of this token.")
    private List<String> assetUrls;

    @ApiModelProperty("Any meta data for this token.")
    private Map<String, Object> metaData;

    private String contract;

    private boolean released;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Royalties getRoyalties() {
        return royalties;
    }

    public void setRoyalties(Royalties royalties) {
        this.royalties = royalties;
    }

    public long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public boolean isSeries() {
        return series;
    }

    public void setSeries(boolean series) {
        this.series = series;
    }

    public long getNumberInSeries() {
        return numberInSeries;
    }

    public void setNumberInSeries(long numberInSeries) {
        this.numberInSeries = numberInSeries;
    }

    public String getTransferOptions() {
        return transferOptions;
    }

    public void setTransferOptions(String transferOptions) {
        this.transferOptions = transferOptions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }
}
