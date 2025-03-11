package dev.getelements.elements.dao.mongo.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Reference;

import java.util.Objects;

@Entity(value = "userUid", useDiscriminator = false)
public class MongoUserUid {

    @Id
    private MongoUserUidScheme id;

    @Reference
    private MongoUser user;

    public MongoUserUidScheme getId() {
        return id;
    }

    public void setId(MongoUserUidScheme id) {
        this.id = id;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user);
    }

    @Override
    public String toString() {
        return "MongoUserUid{" +
                "id=" + id +
                ", user='" + user + '\'' +
                '}';
    }
}
