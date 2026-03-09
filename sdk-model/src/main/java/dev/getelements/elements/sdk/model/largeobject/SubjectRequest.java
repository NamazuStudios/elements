package dev.getelements.elements.sdk.model.largeobject;

import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/** Represents a request specifying which subjects (users, profiles, or wildcards) may perform an operation. */
@Schema
public class SubjectRequest {

    /** Creates a new instance. */
    public SubjectRequest() {}

    @Schema(description = "Flag to check who may perform the operation. If true, all anonymous users may perform the operation.")
    private boolean wildcard;

    @NotNull
    @Schema(description = "A List of all UserIds which can operate against the LargeObject.")
    private List<String> userIds;

    @NotNull
    @Schema(description = "A List of all ProfileIds which can operate against the LargeObject.")
    private List<String> profileIds;

    /**
     * Gets a default valid {@link SubjectRequest} with wildcard set to false and empty ID lists.
     *
     * @return the default subject request
     */
    public static SubjectRequest newDefaultRequest() {
        final var request = new SubjectRequest();
        request.setWildcard(false);
        request.setUserIds(List.of());
        request.setProfileIds(List.of());
        return request;
    }

    /**
     * Gets a wildcard {@link SubjectRequest} that permits all users to perform the operation.
     *
     * @return the wildcard subject request
     */
    public static SubjectRequest newWildcardRequest() {
        final var request = newDefaultRequest();
        request.setWildcard(true);
        return request;
    }

    /**
     * Returns whether this request is a wildcard that allows all anonymous users.
     *
     * @return true if wildcard
     */
    public boolean isWildcard() {
        return wildcard;
    }

    /**
     * Sets whether this request is a wildcard that allows all anonymous users.
     *
     * @param wildcard true if wildcard
     */
    public void setWildcard(boolean wildcard) {
        this.wildcard = wildcard;
    }

    /**
     * Returns the list of user IDs allowed to operate against the large object.
     *
     * @return the user IDs
     */
    public List<String> getUserIds() {
        return userIds;
    }

    /**
     * Sets the list of user IDs allowed to operate against the large object.
     *
     * @param userIds the user IDs
     */
    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    /**
     * Returns the list of profile IDs allowed to operate against the large object.
     *
     * @return the profile IDs
     */
    public List<String> getProfileIds() {
        return profileIds;
    }

    /**
     * Sets the list of profile IDs allowed to operate against the large object.
     *
     * @param profileIds the profile IDs
     */
    public void setProfileIds(List<String> profileIds) {
        this.profileIds = profileIds;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectRequest that = (SubjectRequest) o;
        return isWildcard() == that.isWildcard() && Objects.equals(getUserIds(), that.getUserIds()) && Objects.equals(getProfileIds(), that.getProfileIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isWildcard(), getUserIds(), getProfileIds());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SubjectRequest{");
        sb.append("allUsers=").append(wildcard);
        sb.append(", userIds=").append(userIds);
        sb.append(", profileIds=").append(profileIds);
        sb.append('}');
        return sb.toString();
    }

}
