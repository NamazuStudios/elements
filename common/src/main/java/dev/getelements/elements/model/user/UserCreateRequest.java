package dev.getelements.elements.model.user;

import dev.getelements.elements.Constants;
import dev.getelements.elements.model.profile.CreateProfileSignupRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

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

    @ApiModelProperty("The user's level to assign. Depending on the usage, the server may ignore this field and " +
            "assign its own value.")
    private User.Level level;

    @ApiModelProperty("A list of profiles to assign to this user during creation. The server will attempt to create " +
            "a profile for each item in this list.")
    private List<CreateProfileSignupRequest> profiles;

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

    public List<CreateProfileSignupRequest> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<CreateProfileSignupRequest> profiles) {
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
