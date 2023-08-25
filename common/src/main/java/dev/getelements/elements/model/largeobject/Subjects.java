package dev.getelements.elements.model.largeobject;

import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel
public class Subjects {

    @ApiModelProperty("Flag to check who may perform the operations. True if anyone can.")
    private boolean anonymous;

    @ApiModelProperty("Users which may perform the operations.")
    private List<User> users;

    @ApiModelProperty("Profiles, which owners may perform the operations.")
    private List<Profile> profiles;

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
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
}
