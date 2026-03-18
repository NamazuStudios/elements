package dev.getelements.elements.sdk.model.blockchain.contract.near;



import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Represents metadata for a NEAR protocol transaction execution. */
public class NearMetadata {

    /** Creates a new instance. */
    public NearMetadata() {}

    @Schema(description = "version")
    private long version;

    @Schema(description = "gas_profile")
    private List<NearGasProfile> gasProfile;

    /**
     * Returns the version of the metadata.
     *
     * @return the version
     */
    public long getVersion() {
        return version;
    }

    /**
     * Sets the version of the metadata.
     *
     * @param version the version
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * Returns the gas profile entries.
     *
     * @return the gas profile
     */
    public List<NearGasProfile> getGasProfile() {
        return gasProfile;
    }

    /**
     * Sets the gas profile entries.
     *
     * @param gasProfile the gas profile
     */
    public void setGasProfile(List<NearGasProfile> gasProfile) {
        this.gasProfile = gasProfile;
    }
}
