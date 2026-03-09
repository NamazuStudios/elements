package dev.getelements.elements.sdk.model.application;

import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.util.Map;
import java.util.Objects;

/**
 * Configures the matchmaking system for the application. Defines the queue name and the
 * matchmaker to use when finding players to match. Currently FIFO is builtin and is the default.
 */
@Schema(
    description =
            "This configures the matchmaking system for the application. This defines the queue name and the " +
            "Matchmacker to use when finding players to match. Currently FIFO is builtin and is the default."
)
public class MatchmakingApplicationConfiguration extends ApplicationConfiguration {

    /** Creates a new instance. */
    public MatchmakingApplicationConfiguration() {}

    /**
     * Indicates the minimum number of profiles that can can be capped per match.
     */
    public static final int MIN_PROFILE_CAP = 2;

    /** Default number of seconds a match will linger after expiration. */
    public static final int DEFAULT_MATCH_LINGER = 300; // 5 minutes

    /** Default number of seconds before a match is automatically deleted. */
    public static final int DEFAULT_MATCH_TIMEOUT = 86400; // 24 hours

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
    private int lingerSeconds = DEFAULT_MATCH_LINGER;

    @Min(0)
    @Schema(description = "The absolute timeout of the match. A match will be automatically deleted after this amount of time.")
    private int timeoutSeconds = DEFAULT_MATCH_TIMEOUT;

    @Schema(description = "The metadata for this matchmaking configuration. This will be copied to the match when it is created.")
    private Map<String, Object> metadata;

    @Valid
    @Schema(description = "The metadata spec for this matchmaking configuration. This defines the structure of the metadata.")
    private MetadataSpec metadataSpec;

    /**
     * Returns the callback definition for a successful match.
     * @return the success callback
     */
    public CallbackDefinition getSuccess() {
        return success;
    }

    /**
     * Sets the callback definition for a successful match.
     * @param success the success callback
     */
    public void setSuccess(CallbackDefinition success) {
        this.success = success;
    }

    /**
     * Returns the matchmaker service reference.
     * @return the matchmaker
     */
    public ElementServiceReference getMatchmaker() {
        return matchmaker;
    }

    /**
     * Sets the matchmaker service reference.
     * @param matchmaker the matchmaker
     */
    public void setMatchmaker(ElementServiceReference matchmaker) {
        this.matchmaker = matchmaker;
    }

    /**
     * Returns the maximum number of profiles per match.
     * @return the maximum profile count
     */
    public int getMaxProfiles() {
        return maxProfiles;
    }

    /**
     * Sets the maximum number of profiles per match.
     * @param maxProfiles the maximum profile count
     */
    public void setMaxProfiles(int maxProfiles) {
        this.maxProfiles = maxProfiles;
    }

    /**
     * Returns the match metadata.
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the match metadata.
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the number of seconds a match lingers after expiration.
     * @return the linger seconds
     */
    public int getLingerSeconds() {
        return lingerSeconds;
    }

    /**
     * Sets the number of seconds a match lingers after expiration.
     * @param lingerSeconds the linger seconds
     */
    public void setLingerSeconds(int lingerSeconds) {
        this.lingerSeconds = lingerSeconds;
    }

    /**
     * Returns the absolute match timeout in seconds.
     * @return the timeout seconds
     */
    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    /**
     * Sets the absolute match timeout in seconds.
     * @param timeoutSeconds the timeout seconds
     */
    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * Returns the metadata spec for this configuration.
     * @return the metadata spec
     */
    public MetadataSpec getMetadataSpec() {
        return metadataSpec;
    }

    /**
     * Sets the metadata spec for this configuration.
     * @param metadataSpec the metadata spec
     */
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
