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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCreateResponse that = (UserCreateResponse) o;
        return isActive() == that.isActive() && Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getEmail(), that.getEmail()) && getLevel() == that.getLevel() && Objects.equals(getFacebookId(), that.getFacebookId()) && Objects.equals(getFirebaseId(), that.getFirebaseId()) && Objects.equals(getAppleSignInId(), that.getAppleSignInId()) && Objects.equals(getProfiles(), that.getProfiles()) && Objects.equals(getPassword(), that.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getEmail(), getLevel(), isActive(), getFacebookId(), getFirebaseId(), getAppleSignInId(), getProfiles(), getPassword());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserCreateResponse{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", level=").append(level);
        sb.append(", active=").append(active);
        sb.append(", facebookId='").append(facebookId).append('\'');
        sb.append(", firebaseId='").append(firebaseId).append('\'');
        sb.append(", appleSignInId='").append(appleSignInId).append('\'');
        sb.append(", profiles=").append(profiles);
        sb.append(", password='").append(password).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
