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

    @Valid
    @Schema(description = "The callback definition for when a successful match is made.")
    private CallbackDefinition success;

    @Valid
    @Schema(description = "Describes the matchmaker (dev.getelements.elements.sdk.dao.Matchmaker) to use for this configuration.")
    private ElementServiceReference matchmaker;

    @Min(0)
    @Schema(description = "The maximum number of players that can be matched in a single match. ")
    private int maxPlayers;

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

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
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
        return maxPlayers == that.maxPlayers &&
                Objects.equals(success, that.success) &&
                Objects.equals(matchmaker, that.matchmaker) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(metadataSpec, that.metadataSpec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), success, matchmaker, maxPlayers, metadata, metadataSpec);
    }

    @Override
    public String toString() {
        return "MatchmakingApplicationConfiguration{" +
                "success=" + success +
                ", matchmaker=" + matchmaker +
                ", maxPlayers=" + maxPlayers +
                ", metadata=" + metadata +
                ", metadataSpec=" + metadataSpec +
                '}';
    }

}
