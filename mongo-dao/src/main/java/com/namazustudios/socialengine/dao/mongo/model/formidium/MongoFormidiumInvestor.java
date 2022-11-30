package com.namazustudios.socialengine.dao.mongo.model.formidium;

import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import dev.morphia.annotations.*;

import java.util.Objects;

@Entity(value = "formidium_user", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field(value = "user"))
    })
public class MongoFormidiumInvestor {

    @Id
    private MongoFormidiumInvestorId id;

    @Reference
    private MongoUser user;

    @Property
    private String formidiumInvestorId;

    public MongoFormidiumInvestorId getId() {
        return id;
    }

    public void setId(MongoFormidiumInvestorId id) {
        this.id = id;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
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
        MongoFormidiumInvestor that = (MongoFormidiumInvestor) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getUser(), that.getUser()) && Objects.equals(getFormidiumInvestorId(), that.getFormidiumInvestorId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUser(), getFormidiumInvestorId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoFormidiumInvestor{");
        sb.append("id=").append(id);
        sb.append(", user=").append(user);
        sb.append(", formidiumInvestorId='").append(formidiumInvestorId).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
