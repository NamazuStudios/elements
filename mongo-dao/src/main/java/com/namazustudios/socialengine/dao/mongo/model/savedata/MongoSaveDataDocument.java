package com.namazustudios.socialengine.dao.mongo.model.savedata;

import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;

import java.security.Timestamp;

@Entity(value = "save_data", useDiscriminator = false)
@Indexes({
    @Index(fields = {@Field("user"), @Field("profile")}),
    @Index(fields = @Field(value = "contents", type = IndexType.TEXT))
})
public class MongoSaveDataDocument {

    @Id
    private MongoSaveDataDocumentId id;

    @Property
    private String version;

    @Property
    private Timestamp timestamp;

    @Reference
    private MongoProfile profile;

    public MongoSaveDataDocumentId getId() {
        return id;
    }

    public void setId(MongoSaveDataDocumentId id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public MongoProfile getProfile() {
        return profile;
    }

    public void setProfile(MongoProfile profile) {
        this.profile = profile;
    }

}
