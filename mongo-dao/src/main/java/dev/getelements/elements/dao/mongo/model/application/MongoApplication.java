package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.Objects;

/**
 * Created by patricktwohig on 7/10/15.
 */
@Entity(value = "application", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("name"), options = @IndexOptions(unique = true, sparse = true))
})
public class MongoApplication {

    @Id
    private ObjectId objectId;

    @Property
    private String name;

    @Property
    private String description;

    @Property
    private Map<String, Object> attributes;

    @Property
    private Integer maxProfiles;

    @Property
    private Boolean autoCreateProfile;

    @Property
    private Boolean authoritativeProfilePicture;

    @Property
    private String displayNameRegex;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Integer getMaxProfiles() {
        return maxProfiles;
    }

    public void setMaxProfiles(Integer maxProfiles) {
        this.maxProfiles = maxProfiles;
    }

    public Boolean getAutoCreateProfile() {
        return autoCreateProfile;
    }

    public void setAutoCreateProfile(Boolean autoCreateProfile) {
        this.autoCreateProfile = autoCreateProfile;
    }

    public Boolean getAuthoritativeProfilePicture() {
        return authoritativeProfilePicture;
    }

    public void setAuthoritativeProfilePicture(Boolean authoritativeProfilePicture) {
        this.authoritativeProfilePicture = authoritativeProfilePicture;
    }

    public String getDisplayNameRegex() {
        return displayNameRegex;
    }

    public void setDisplayNameRegex(String displayNameRegex) {
        this.displayNameRegex = displayNameRegex;
    }

}
