package dev.getelements.elements.sdk.model.blockchain.contract.near;



import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class NearMetadata {

    @Schema(description = "version")
    private long version;

    @Schema(description = "gas_profile")
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
