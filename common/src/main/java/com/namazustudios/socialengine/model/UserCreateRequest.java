package com.namazustudios.socialengine.model;

import com.namazustudios.socialengine.Constants;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * Represents a user create request
 *
 * Created by davidjbrooks on 12/11/2018.
 */
@ApiModel
public class UserCreateRequest implements Serializable {

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String name;

    @NotNull
    @Pattern(regexp = Constants.Regexp.EMAIL_ADDRESS)
    private String email;

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String password;

    /**
     * Gets the user's login name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the user's password.
     *
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user's password.
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the user's login name.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Gets the user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCreateRequest)) return false;

        UserCreateRequest userCreateRequest = (UserCreateRequest) o;

        if (getName() != null ? !getName().equals(userCreateRequest.getName()) : userCreateRequest.getName() != null) return false;
        if (getPassword() != null ? !getPassword().equals(userCreateRequest.getPassword()) : userCreateRequest.getPassword() != null) return false;
        return (getEmail() != null ? !getEmail().equals(userCreateRequest.getEmail()) : userCreateRequest.getEmail() != null);
    }

    @Override
    public int hashCode() {
        int result = (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getPassword() != null ? getPassword().hashCode() : 0);
        result = 31 * result + (getEmail() != null ? getEmail().hashCode() : 0);
        return result;
    }

}
