package dev.getelements.elements.sdk.model.user;

import dev.getelements.elements.sdk.model.profile.Profile;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class UserCreateResponse {

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public User.Level getLevel() {
        return level;
    }

    public void setLevel(User.Level level) {
        this.level = level;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getAppleSignInId() {
        return appleSignInId;
    }

    public void setAppleSignInId(String appleSignInId) {
        this.appleSignInId = appleSignInId;
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPrimaryPhoneNb() {
        return primaryPhoneNb;
    }

    public void setPrimaryPhoneNb(String primaryPhoneNb) {
        this.primaryPhoneNb = primaryPhoneNb;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<String> getLinkedAccounts() {
        return linkedAccounts;
    }

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
