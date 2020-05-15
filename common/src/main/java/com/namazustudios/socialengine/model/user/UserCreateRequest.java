package com.namazustudios.socialengine.model.user;

import com.namazustudios.socialengine.Constants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Objects;

@ApiModel
public class UserCreateRequest implements Serializable {

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String name;

    @NotNull
    @Pattern(regexp = Constants.Regexp.EMAIL_ADDRESS)
    private String email;

    @ApiModelProperty("The user's plaintext password, only to be provided in POST/PUT requests in the User Resource " +
            "REST API interface. In the future, a dedicated REST API model may be constructed instead of using a " +
            "direct User model.")
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String password;

    private User.Level level;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCreateRequest that = (UserCreateRequest) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(email, that.email) &&
                Objects.equals(password, that.password) &&
                level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, password, level);
    }

    @Override
    public String toString() {
        return "UserCreateRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", level=" + level +
                '}';
    }

}
