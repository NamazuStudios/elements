package com.namazustudios.socialengine.dao.mongo.model.formidium;

import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import dev.morphia.annotations.*;

import java.util.Objects;

@Entity(value = "formidium_user", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field(value = "_id.userId")),
        @Index(fields = @Field(value = "_id.formidiumInvestorId")),
        @Index(fields = {
                @Field(value = "_id.userId"),
                @Field(value = "_id.formidiumInvestorId")
        })
})
public class MongoFormidiumUser {

    @Id
    private MongoFormidiumUserId id;

    @Reference
    private MongoUser user;

    public MongoFormidiumUserId getId() {
        return id;
    }

    public void setId(MongoFormidiumUserId id) {
        this.id = id;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoFormidiumUser that = (MongoFormidiumUser) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoFormidiumUser{");
        sb.append("id=").append(id);
        sb.append(", user=").append(user);
        sb.append('}');
        return sb.toString();
    }

}
