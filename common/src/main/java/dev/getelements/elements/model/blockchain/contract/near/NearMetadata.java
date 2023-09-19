package dev.getelements.elements.model.blockchain.contract.near;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class NearMetadata {

    @ApiModelProperty("version")
    private long version;

    @ApiModelProperty("gas_profile")
    private List<NearGasProfile> gasProfile;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public List<NearGasProfile> getGasProfile() {
        return gasProfile;
    }

    public void setGasProfile(List<NearGasProfile> gasProfile) {
        this.gasProfile = gasProfile;
    }
}
