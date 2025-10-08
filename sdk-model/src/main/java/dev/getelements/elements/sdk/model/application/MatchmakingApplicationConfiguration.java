package dev.getelements.elements.sdk.model.application;

import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.util.Map;
import java.util.Objects;

@Schema(
    description =
            "This configures the matchmaking system for the application. This defines the queue name and the " +
            "Matchmacker to use when finding players to match. Currently FIFO is builtin and is the default."
)
public class MatchmakingApplicationConfiguration extends ApplicationConfiguration {

    /**
     * Indicates the minimum number of profiles that can can be capped per match.
     */
    public static final int MIN_PROFILE_CAP = 2;

    @Valid
    @Schema(description = "The callback definition for when a successful match is made.")
    private CallbackDefinition success;

    @Valid
    @Schema(description = "Describes the matchmaker (dev.getelements.elements.sdk.dao.Matchmaker) to use for this configuration.")
    private ElementServiceReference matchmaker;

    @Min(MIN_PROFILE_CAP)
    @Schema(description = "The maximum number of profiles that can be matched in a single match. ")
    private int maxProfiles = MIN_PROFILE_CAP;

    @Min(0)
    @Schema(description = "The amount of time a match will linger after it is marked as expired.")
    private int lingerSeconds;

    @Min(0)
    @Schema(description = "The absolute timeout of the match. A match will be automatically deleted after this amount of time.")
    private int timeoutSeconds;

    @Schema(description = "The metadata for this matchmaking configuration. This will be copied to the match when it is created.")
    private Map<String, Object> metadata;

    @Valid
    @Schema(description = "The metadata spec for this matchmaking configuration. This defines the structure of the metadata.")
    private MetadataSpec metadataSpec;

    public CallbackDefinition getSuccess() {
        return success;
    }

    public void setSuccess(CallbackDefinition success) {
        this.success = success;
    }

    public ElementServiceReference getMatchmaker() {
        return matchmaker;
    }

    public void setMatchmaker(ElementServiceReference matchmaker) {
        this.matchmaker = matchmaker;
    }

    public int getMaxProfiles() {
        return maxProfiles;
    }

    public void setMaxProfiles(int maxProfiles) {
        this.maxProfiles = maxProfiles;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public int getLingerSeconds() {
        return lingerSeconds;
    }

    public void setLingerSeconds(int lingerSeconds) {
        this.lingerSeconds = lingerSeconds;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public MetadataSpec getMetadataSpec() {
        return metadataSpec;
    }

    public void setMetadataSpec(MetadataSpec metadataSpec) {
        this.metadataSpec = metadataSpec;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MatchmakingApplicationConfiguration that = (MatchmakingApplicationConfiguration) o;
        return maxProfiles == that.maxProfiles &&
                Objects.equals(success, that.success) &&
                Objects.equals(matchmaker, that.matchmaker) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(metadataSpec, that.metadataSpec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), success, matchmaker, maxProfiles, metadata, metadataSpec);
    }

    @Override
    public String toString() {
        return "MatchmakingApplicationConfiguration{" +
                "success=" + success +
                ", matchmaker=" + matchmaker +
                ", maxPlayers=" + maxProfiles +
                ", metadata=" + metadata +
                ", metadataSpec=" + metadataSpec +
                '}';
    }

}
