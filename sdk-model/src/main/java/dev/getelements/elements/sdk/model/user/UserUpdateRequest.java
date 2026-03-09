package dev.getelements.elements.sdk.model.user;

import dev.getelements.elements.sdk.model.Constants;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;

/** Represents a request to update an existing user's properties. */
@Schema
public class UserUpdateRequest {

    /** Creates a new instance. */
    public UserUpdateRequest() {}

    @NotNull
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String name;

    @NotNull
    @Pattern(regexp = Constants.Regexp.EMAIL_ADDRESS)
    private String email;

    @Pattern(regexp = Constants.Regexp.PHONE_NB)
    private String primaryPhoneNb;

    @Pattern(regexp = Constants.Regexp.FIRST_NAME)
    private String firstName;

    @Pattern(regexp = Constants.Regexp.LAST_NAME)
    private String lastName;

    @Schema(description = "The user's plaintext password, only to be provided in POST/PUT requests in the User Resource " +
            "REST API interface. In the future, a dedicated REST API model may be constructed instead of using a " +
            "direct User model.")
    @Pattern(regexp = Constants.Regexp.NO_WHITE_SPACE)
    private String password;

    @NotNull
    private User.Level level;

    /**
     * Returns the unique login name for the user.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique login name for the user.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the email address for the user.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address for the user.
     *
     * @param email the email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the plaintext password for the user.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the plaintext password for the user.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the access level to assign to the user.
     *
     * @return the level
     */
    public User.Level getLevel() {
        return level;
    }

    /**
     * Sets the access level to assign to the user.
     *
     * @param level the level
     */
    public void setLevel(User.Level level) {
        this.level = level;
    }

    /**
     * Returns the primary phone number for the user.
     *
     * @return the primary phone number
     */
    public String getPrimaryPhoneNb() {
        return primaryPhoneNb;
    }

    /**
     * Sets the primary phone number for the user.
     *
     * @param primaryPhoneNb the primary phone number
     */
    public void setPrimaryPhoneNb(String primaryPhoneNb) {
        this.primaryPhoneNb = primaryPhoneNb;
    }

    /**
     * Returns the first name of the user.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the user.
     *
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name of the user.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the user.
     *
     * @param lastName the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserUpdateRequest that = (UserUpdateRequest) o;
        return Objects.equals(name, that.name) && Objects.equals(email, that.email) && Objects.equals(primaryPhoneNb, that.primaryPhoneNb) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(password, that.password) && level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, primaryPhoneNb, firstName, lastName, password, level);
    }

    @Override
    public String toString() {
        return "UserUpdateRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", primaryPhoneNb='" + primaryPhoneNb + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", password='" + password + '\'' +
                ", level=" + level +
                '}';
    }
}
