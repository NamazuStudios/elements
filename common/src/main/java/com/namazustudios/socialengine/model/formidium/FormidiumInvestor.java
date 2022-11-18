package com.namazustudios.socialengine.model.formidium;

import com.namazustudios.socialengine.model.user.User;
import io.swagger.annotations.ApiModel;

import java.io.Serializable;
import java.util.Objects;

@ApiModel
public class FormidiumInvestor implements Serializable {

    private User user;

    private String investorId;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getInvestorId() {
        return investorId;
    }

    public void setInvestorId(String investorId) {
        this.investorId = investorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormidiumInvestor that = (FormidiumInvestor) o;
        return Objects.equals(getUser(), that.getUser()) && Objects.equals(getInvestorId(), that.getInvestorId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getInvestorId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FormidiumInvestor{");
        sb.append("user=").append(user);
        sb.append(", investorId='").append(investorId).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
