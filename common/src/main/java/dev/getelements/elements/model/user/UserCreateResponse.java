package dev.getelements.elements.model.user;

import dev.getelements.elements.model.profile.Profile;

import java.util.List;
import java.util.Objects;

public class UserCreateResponse {

    private String id;

    private String name;

    private String email;

    private User.Level level;

    private boolean active;

    private String facebookId;

    private String firebaseId;

    private String appleSignInId;

    private String phoneNb;

    private List<Profile> profiles;

    private String password;

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

    public String getPhoneNb() {
        return phoneNb;
    }

    public void setPhoneNb(String phoneNb) {
        this.phoneNb = phoneNb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCreateResponse that = (UserCreateResponse) o;
        return active == that.active && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(email, that.email) && level == that.level && Objects.equals(facebookId, that.facebookId) && Objects.equals(firebaseId, that.firebaseId) && Objects.equals(appleSignInId, that.appleSignInId) && Objects.equals(phoneNb, that.phoneNb) && Objects.equals(profiles, that.profiles) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, level, active, facebookId, firebaseId, appleSignInId, phoneNb, profiles, password);
    }

    @Override
    public String toString() {
        return "UserCreateResponse{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", level=" + level +
                ", active=" + active +
                ", facebookId='" + facebookId + '\'' +
                ", firebaseId='" + firebaseId + '\'' +
                ", appleSignInId='" + appleSignInId + '\'' +
                ", phoneNb='" + phoneNb + '\'' +
                ", profiles=" + profiles +
                ", password='" + password + '\'' +
                '}';
    }
}
