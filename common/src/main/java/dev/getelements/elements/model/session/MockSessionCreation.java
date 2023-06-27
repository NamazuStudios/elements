package dev.getelements.elements.model.session;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The mock session creation.")
public class MockSessionCreation extends SessionCreation {

    @ApiModelProperty("The assocaited mock user will automatically be deleted at supplied time.")
    private long userExpiresAt;

    @ApiModelProperty("The randomly-assigned password for the mock user.")
    private String password;

    public long getUserExpiresAt() {
        return userExpiresAt;
    }

    public void setUserExpiresAt(long userExpiresAt) {
        this.userExpiresAt = userExpiresAt;
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
        if (!(o instanceof MockSessionCreation)) return false;

        MockSessionCreation that = (MockSessionCreation) o;

        if (getUserExpiresAt() != that.getUserExpiresAt()) return false;
        return getPassword() != null ? getPassword().equals(that.getPassword()) : that.getPassword() == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (getUserExpiresAt() ^ (getUserExpiresAt() >>> 32));
        result = 31 * result + (getPassword() != null ? getPassword().hashCode() : 0);
        return result;
    }

}
