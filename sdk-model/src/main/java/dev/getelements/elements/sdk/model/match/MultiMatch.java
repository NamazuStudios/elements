package dev.getelements.elements.sdk.model.match;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.Map;
import java.util.Objects;

@Schema(description = "Represents a multi-match in the matchmaking system.")
public class MultiMatch {

    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @Schema(description = "The unique ID of the match.")
    private String id;

    @NotNull
    @NotNull
    @Schema(description = "The status of the match.")
    private MultiMatchStatus status;

    @NotNull
    @Schema(description = "The matchmaking configuration for this multi-match.")
    private MatchmakingApplicationConfiguration configuration;

    @Schema(description = "The metadata of hte multi-match, which can be used to store additional information about the match.")
    private Map<String, Object> metadata;

    @Schema(description = "The expiry time of the match in seconds. If not set, the match will not expire.")
    private Long expiry;

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

    public Long getExpiry() {
        return expiry;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        MultiMatch that = (MultiMatch) object;
        return Objects.equals(getId(), that.getId()) && getStatus() == that.getStatus() && Objects.equals(getConfiguration(), that.getConfiguration()) && Objects.equals(getMetadata(), that.getMetadata()) && Objects.equals(getExpiry(), that.getExpiry());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getStatus(), getConfiguration(), getMetadata(), getExpiry());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MultiMatch{");
        sb.append("id='").append(id).append('\'');
        sb.append(", status=").append(status);
        sb.append(", configuration=").append(configuration);
        sb.append(", metadata=").append(metadata);
        sb.append(", expiry=").append(expiry);
        sb.append('}');
        return sb.toString();
    }

}
