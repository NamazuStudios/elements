package dev.getelements.elements.dao.mongo.model;

import org.bson.types.ObjectId;
import dev.morphia.annotations.*;

@Entity(value = "fcm_registration", useDiscriminator = false)
@Indexes({
    @Index(fields = @Field("profile"), options = @IndexOptions(unique = true))
})
public class MongoFCMRegistration {

    @Id
    private ObjectId objectId;

    @Reference
    private MongoProfile profile;

    @Property
    private String registrationToken;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public MongoProfile getProfile() {
        return profile;
    }

    public void setProfile(MongoProfile profile) {
        this.profile = profile;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MongoFCMRegistration)) return false;

        MongoFCMRegistration that = (MongoFCMRegistration) o;

        if (getObjectId() != null ? !getObjectId().equals(that.getObjectId()) : that.getObjectId() != null)
            return false;
        if (getProfile() != null ? !getProfile().equals(that.getProfile()) : that.getProfile() != null) return false;
        return getRegistrationToken() != null ? getRegistrationToken().equals(that.getRegistrationToken()) : that.getRegistrationToken() == null;
    }

    @Override
    public int hashCode() {
        int result = getObjectId() != null ? getObjectId().hashCode() : 0;
        result = 31 * result + (getProfile() != null ? getProfile().hashCode() : 0);
        result = 31 * result + (getRegistrationToken() != null ? getRegistrationToken().hashCode() : 0);
        return result;
    }

}
