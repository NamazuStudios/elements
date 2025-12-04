package dev.getelements.elements.dao.mongo.model;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

import java.util.Objects;

@Entity(value = "userUidScheme", useDiscriminator = false)
public class MongoUserUidScheme {

    @Property
    private String scheme;

    @Property
    private String id;

    public MongoUserUidScheme() {}

    public MongoUserUidScheme(final String scheme, final String id) {
        this.scheme = scheme;
        this.id = id;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheme, id);
    }

    @Override
    public String toString() {
        return "UserUid{" +
                "scheme='" + scheme + '\'' +
                ", id='" + id + '\'' +
                "}";
    }

}
