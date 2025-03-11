package dev.getelements.elements.sdk.model.largeobject;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

@Schema
public class Subjects {

    @Schema(description = "Flag to check who may perform the operations. True if all users may access the object.")
    private boolean wildcard;

    @NotNull
    @Schema(description = "Users which may perform the operations.")
    private List<User> users;

    @NotNull
    @Schema(description = "Profiles, which owners may perform the operations.")
    private List<Profile> profiles;

    /**
     * Fetches an anonymous subject which permits all access.
     */
    public static Subjects wildcardSubject() {
        Subjects anonymousSubject = new Subjects();
        anonymousSubject.wildcard = true;
        anonymousSubject.setUsers(emptyList());
        anonymousSubject.setProfiles(emptyList());
        return anonymousSubject;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public void setWildcard(boolean wildcard) {
        this.wildcard = wildcard;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subjects subjects = (Subjects) o;
        return isWildcard() == subjects.isWildcard() && Objects.equals(getUsers(), subjects.getUsers()) && Objects.equals(getProfiles(), subjects.getProfiles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isWildcard(), getUsers(), getProfiles());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Subjects{");
        sb.append("allUsers=").append(wildcard);
        sb.append(", users=").append(users);
        sb.append(", profiles=").append(profiles);
        sb.append('}');
        return sb.toString();
    }

}
