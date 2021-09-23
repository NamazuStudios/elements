package com.namazustudios.socialengine.model.user;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.model.profile.CreateProfileRequest;
import com.namazustudios.socialengine.model.profile.ProfileSignupRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@ApiModel
public class UserCreateRequest implements Serializable {

    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String name;

    @Pattern(regexp = Constants.Regexp.EMAIL_ADDRESS)
    private String email;

    @ApiModelProperty("The user's plaintext password, only to be provided in POST/PUT requests in the User Resource " +
            "REST API interface. In the future, a dedicated REST API model may be constructed instead of using a " +
            "direct User model.")
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String password;

    private User.Level level;

    private List<ProfileSignupRequest> profiles;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User.Level getLevel() {
        return level;
    }

    public void setLevel(User.Level level) {
        this.level = level;
    }

    public List<ProfileSignupRequest> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<ProfileSignupRequest> profiles) {
        this.profiles = profiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCreateRequest that = (UserCreateRequest) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getEmail(), that.getEmail()) && Objects.equals(getPassword(), that.getPassword()) && getLevel() == that.getLevel() && Objects.equals(getProfiles(), that.getProfiles());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getEmail(), getPassword(), getLevel(), getProfiles());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserCreateRequest{");
        sb.append("name='").append(name).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", level=").append(level);
        sb.append(", profiles=").append(profiles);
        sb.append('}');
        return sb.toString();
    }

}
