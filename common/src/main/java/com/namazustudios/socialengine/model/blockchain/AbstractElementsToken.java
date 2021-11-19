package com.namazustudios.socialengine.model.blockchain;

import org.web3j.abi.datatypes.generated.Uint160;

import java.util.List;

public abstract class AbstractElementsToken {

    private Uint160 owner;

    private String name;

    private String description;

    private List<String> tags;

    private long totalSupply;

    private String accessOption;

    private String previewUrl;

    private List<String> assetUrls;

    public Uint160 getOwner() {
        return owner;
    }

    public void setOwner(Uint160 owner) {
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public long getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(long totalSupply) {
        this.totalSupply = totalSupply;
    }

    public String getAccessOption() {
        return accessOption;
    }

    public void setAccessOption(String accessOption) {
        this.accessOption = accessOption;
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
}
