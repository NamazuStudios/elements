package dev.getelements.elements.dao.mongo.model.largeobject;

import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import java.util.List;
import java.util.Objects;

@Embedded
public class MongoSubjects {

    @Property
    private boolean anonymous;

    @Property
    private List<MongoUser> users;

    @Property
    private List<MongoProfile> profiles;

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public List<MongoUser> getUsers() {
        return users;
    }

    public void setUsers(List<MongoUser> users) {
        this.users = users;
    }

    public List<MongoProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<MongoProfile> profiles) {
        this.profiles = profiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoSubjects that = (MongoSubjects) o;
        return anonymous == that.anonymous && Objects.equals(users, that.users) && Objects.equals(profiles, that.profiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anonymous, users, profiles);
    }
}
