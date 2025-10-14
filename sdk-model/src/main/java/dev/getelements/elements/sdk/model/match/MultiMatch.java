package dev.getelements.elements.sdk.model.match;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.Map;
import java.util.Objects;

import static dev.getelements.elements.sdk.model.match.MultiMatchStatus.OPEN;

@Schema(description = "Represents a multi-match in the matchmaking system.")
public class MultiMatch {

    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @Schema(description = "The unique ID of the match.")
    private String id;

    @NotNull
    @NotNull
    @Schema(description = "The status of the match.")
    private MultiMatchStatus status = OPEN;

    @NotNull
    @Schema(description = "The matchmaking configuration for this multi-match.")
    private MatchmakingApplicationConfiguration configuration;

    @Schema(description = "The metadata of hte multi-match, which can be used to store additional information about the match.")
    private Map<String, Object> metadata;

    @Schema(description = "The number of players currently in the match.")
    private int count;

    @Schema(description = "The expiry time of the match in seconds. If not set, the match will not expire.")
    private Long expiry;

    @Schema(description = "The timestamp at which the match was created, in milliseconds since epoch.")
    private Long created;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MultiMatchStatus getStatus() {
        return status;
    }

    public void setStatus(MultiMatchStatus status) {
        this.status = status;
    }

    public MatchmakingApplicationConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(MatchmakingApplicationConfiguration configuration) {
        this.configuration = configuration;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Long getExpiry() {
        return expiry;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        MultiMatch that = (MultiMatch) object;
        return count == that.count && Objects.equals(id, that.id) && status == that.status && Objects.equals(configuration, that.configuration) && Objects.equals(metadata, that.metadata) && Objects.equals(expiry, that.expiry) && Objects.equals(created, that.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, configuration, metadata, count, expiry, created);
    }

    @Override
    public String toString() {
        return "MultiMatch{" +
                "id='" + id + '\'' +
                ", status=" + status +
                ", configuration=" + configuration +
                ", metadata=" + metadata +
                ", count=" + count +
                ", expiry=" + expiry +
                ", created=" + created +
                '}';
    }

}
