package dev.getelements.elements.sdk.model.user;

import dev.getelements.elements.sdk.model.profile.Profile;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Represents the response returned after successfully creating a user. */
public class UserCreateResponse {

    /** Creates a new instance. */
    public UserCreateResponse() {}

    private String id;

    private String name;

    private String email;

    private String firstName;

    private String lastName;

    private User.Level level;

    private boolean active;

    private String facebookId;

    private String firebaseId;

    private String appleSignInId;

    private String primaryPhoneNb;

    private List<Profile> profiles;

    private String password;

    private Set<String> linkedAccounts;

    /**
     * Returns the database ID of the created user.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the database ID of the created user.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the login name of the created user.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the login name of the created user.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the email address of the created user.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the created user.
     *
     * @param email the email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the access level of the created user.
     *
     * @return the level
     */
    public User.Level getLevel() {
        return level;
    }

    /**
     * Sets the access level of the created user.
     *
     * @param level the level
     */
    public void setLevel(User.Level level) {
        this.level = level;
    }

    /**
     * Returns whether the created user is active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether the created user is active.
     *
     * @param active true if active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the Facebook ID of the created user.
     *
     * @return the Facebook ID
     */
    public String getFacebookId() {
        return facebookId;
    }

    /**
     * Sets the Facebook ID of the created user.
     *
     * @param facebookId the Facebook ID
     */
    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    /**
     * Returns the Firebase ID of the created user.
     *
     * @return the Firebase ID
     */
    public String getFirebaseId() {
        return firebaseId;
    }

    /**
     * Sets the Firebase ID of the created user.
     *
     * @param firebaseId the Firebase ID
     */
    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    /**
     * Returns the Apple Sign-In ID of the created user.
     *
     * @return the Apple Sign-In ID
     */
    public String getAppleSignInId() {
        return appleSignInId;
    }

    /**
     * Sets the Apple Sign-In ID of the created user.
     *
     * @param appleSignInId the Apple Sign-In ID
     */
    public void setAppleSignInId(String appleSignInId) {
        this.appleSignInId = appleSignInId;
    }

    /**
     * Returns the profiles created for this user.
     *
     * @return the profiles
     */
    public List<Profile> getProfiles() {
        return profiles;
    }

    /**
     * Sets the profiles created for this user.
     *
     * @param profiles the profiles
     */
    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    /**
     * Returns the generated password for this user.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the generated password for this user.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the primary phone number of the created user.
     *
     * @return the primary phone number
     */
    public String getPrimaryPhoneNb() {
        return primaryPhoneNb;
    }

    /**
     * Sets the primary phone number of the created user.
     *
     * @param primaryPhoneNb the primary phone number
     */
    public void setPrimaryPhoneNb(String primaryPhoneNb) {
        this.primaryPhoneNb = primaryPhoneNb;
    }

    /**
     * Returns the first name of the created user.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the created user.
     *
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name of the created user.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the created user.
     *
     * @param lastName the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the set of linked account or auth scheme names.
     *
     * @return the linked accounts
     */
    public Set<String> getLinkedAccounts() {
        return linkedAccounts;
    }

    /**
     * Sets the set of linked account or auth scheme names.
     *
     * @param linkedAccounts the linked accounts
     */
    public void setLinkedAccounts(Set<String> linkedAccounts) {
        this.linkedAccounts = linkedAccounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCreateResponse that = (UserCreateResponse) o;
        return active == that.active && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(email, that.email) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && level == that.level && Objects.equals(facebookId, that.facebookId) && Objects.equals(firebaseId, that.firebaseId) && Objects.equals(appleSignInId, that.appleSignInId) && Objects.equals(primaryPhoneNb, that.primaryPhoneNb) && Objects.equals(profiles, that.profiles) && Objects.equals(password, that.password) && Objects.equals(linkedAccounts, that.linkedAccounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, firstName, lastName, level, active, facebookId, firebaseId, appleSignInId, primaryPhoneNb, profiles, password, linkedAccounts);
    }

    @Override
    public String toString() {
        return "UserCreateResponse{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", level=" + level +
                ", active=" + active +
                ", facebookId='" + facebookId + '\'' +
                ", firebaseId='" + firebaseId + '\'' +
                ", appleSignInId='" + appleSignInId + '\'' +
                ", primaryPhoneNb='" + primaryPhoneNb + '\'' +
                ", profiles=" + profiles +
                ", password='" + password + '\'' +
                ", linkedAccounts='" + linkedAccounts + '\'' +
                '}';
    }
}
