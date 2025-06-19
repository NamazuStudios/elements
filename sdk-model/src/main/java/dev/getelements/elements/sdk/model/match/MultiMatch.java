package dev.getelements.elements.sdk.model.match;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.Map;
import java.util.Objects;

@Schema
public class MultiMatch {

    @NotNull(groups = ValidationGroups.Update.class)
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

    @Schema(description = "Metadata")
    private Map<String, Object> metadata;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MultiMatch that = (MultiMatch) o;
        return
            status == that.status &&
            Objects.equals(id, that.id) &&
            Objects.equals(configuration, that.configuration) &&
            Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, configuration, metadata);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MultiMatch{");
        sb.append("id='").append(id).append('\'');
        sb.append(", status=").append(status);
        sb.append(", configuration=").append(configuration);
        sb.append(", metadata=").append(metadata);
        sb.append('}');
        return sb.toString();
    }

}
