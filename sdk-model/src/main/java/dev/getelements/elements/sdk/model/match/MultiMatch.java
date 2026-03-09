package dev.getelements.elements.sdk.model.match;

import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.ucode.UniqueCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.Map;
import java.util.Objects;

import static dev.getelements.elements.sdk.model.match.MultiMatchStatus.OPEN;

/** Represents a multi-match in the matchmaking system. */
@Schema(description = "Represents a multi-match in the matchmaking system.")
public class MultiMatch {

    /** Creates a new instance. */
    public MultiMatch() {}

    @Null(groups = {ValidationGroups.Insert.class, ValidationGroups.Create.class})
    @Schema(description = "The unique ID of the match.")
    private String id;

    @Schema(description = "The unique join code for the match. May be null if join code is not used.")
    private UniqueCode joinCode;

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

    /**
     * Returns the unique ID of the match.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of the match.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the unique join code for the match.
     *
     * @return the join code
     */
    public UniqueCode getJoinCode() {
        return joinCode;
    }

    /**
     * Sets the unique join code for the match.
     *
     * @param joinCode the join code
     */
    public void setJoinCode(UniqueCode joinCode) {
        this.joinCode = joinCode;
    }

    /**
     * Returns the status of the match.
     *
     * @return the status
     */
    public MultiMatchStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the match.
     *
     * @param status the status
     */
    public void setStatus(MultiMatchStatus status) {
        this.status = status;
    }

    /**
     * Returns the matchmaking configuration for this multi-match.
     *
     * @return the configuration
     */
    public MatchmakingApplicationConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the matchmaking configuration for this multi-match.
     *
     * @param configuration the configuration
     */
    public void setConfiguration(MatchmakingApplicationConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns the metadata of the multi-match.
     *
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata of the multi-match.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the number of players currently in the match.
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the number of players currently in the match.
     *
     * @param count the count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Returns the expiry time of the match in seconds.
     *
     * @return the expiry
     */
    public Long getExpiry() {
        return expiry;
    }

    /**
     * Sets the expiry time of the match in seconds.
     *
     * @param expiry the expiry
     */
    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    /**
     * Returns the timestamp at which the match was created, in milliseconds since epoch.
     *
     * @return the created timestamp
     */
    public Long getCreated() {
        return created;
    }

    /**
     * Sets the timestamp at which the match was created, in milliseconds since epoch.
     *
     * @param created the created timestamp
     */
    public void setCreated(Long created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        MultiMatch that = (MultiMatch) object;
        return count == that.count && Objects.equals(id, that.id) && Objects.equals(joinCode, that.joinCode) && status == that.status && Objects.equals(configuration, that.configuration) && Objects.equals(metadata, that.metadata) && Objects.equals(expiry, that.expiry) && Objects.equals(created, that.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, joinCode, status, configuration, metadata, count, expiry, created);
    }

    @Override
    public String toString() {
        return "MultiMatch{" +
                "id='" + id + '\'' +
                ", joinCode=" + joinCode +
                ", status=" + status +
                ", configuration=" + configuration +
                ", metadata=" + metadata +
                ", count=" + count +
                ", expiry=" + expiry +
                ", created=" + created +
                '}';
    }

}
