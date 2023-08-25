package dev.getelements.elements.model.largeobject;

import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

@ApiModel
public class Subjects {

    @ApiModelProperty("Flag to check who may perform the operations. True if all users may access the object.")
    private boolean allUsers;


    @ApiModelProperty("Flag to check who may perform the operations. True if all profiles may access the object.")
    private boolean allProfiles;

    @NotNull
    @ApiModelProperty("Users which may perform the operations.")
    private List<User> users;

    @NotNull
    @ApiModelProperty("Profiles, which owners may perform the operations.")
    private List<Profile> profiles;

    /**
     * Fetches an anonymous subject which permits all access.
     * @return
     */
    public static Subjects anonymousSubject() {
        Subjects anonymousSubject = new Subjects();
        anonymousSubject.allUsers = true;
        anonymousSubject.allProfiles = true;
        anonymousSubject.setUsers(emptyList());
        anonymousSubject.setProfiles(emptyList());
        return anonymousSubject;
    }

    public boolean isAllUsers() {
        return allUsers;
    }

    public void setAllUsers(boolean allUsers) {
        this.allUsers = allUsers;
    }

    public boolean isAllProfiles() {
        return allProfiles;
    }

    public void setAllProfiles(boolean allProfiles) {
        this.allProfiles = allProfiles;
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
        return isAllUsers() == subjects.isAllUsers() && isAllProfiles() == subjects.isAllProfiles() && Objects.equals(getUsers(), subjects.getUsers()) && Objects.equals(getProfiles(), subjects.getProfiles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isAllUsers(), isAllProfiles(), getUsers(), getProfiles());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Subjects{");
        sb.append("allUsers=").append(allUsers);
        sb.append(", allProfiles=").append(allProfiles);
        sb.append(", users=").append(users);
        sb.append(", profiles=").append(profiles);
        sb.append('}');
        return sb.toString();
    }

}
