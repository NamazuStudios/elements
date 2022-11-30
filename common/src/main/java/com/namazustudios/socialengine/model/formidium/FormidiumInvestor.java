package com.namazustudios.socialengine.model.formidium;

import com.namazustudios.socialengine.model.user.User;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;
import java.util.Objects;

@ApiModel
public class FormidiumInvestor implements Serializable {

    private String id;

    private User user;

    private String formidiumInvestorId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFormidiumInvestorId() {
        return formidiumInvestorId;
    }

    public void setFormidiumInvestorId(String formidiumInvestorId) {
        this.formidiumInvestorId = formidiumInvestorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormidiumInvestor that = (FormidiumInvestor) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getUser(), that.getUser()) && Objects.equals(getFormidiumInvestorId(), that.getFormidiumInvestorId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUser(), getFormidiumInvestorId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FormidiumInvestor{");
        sb.append("id='").append(id).append('\'');
        sb.append(", user=").append(user);
        sb.append(", formidiumInvestorId='").append(formidiumInvestorId).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
