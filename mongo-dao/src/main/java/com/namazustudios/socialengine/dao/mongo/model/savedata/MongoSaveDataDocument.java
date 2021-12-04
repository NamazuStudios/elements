package com.namazustudios.socialengine.dao.mongo.model.savedata;

import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;

import java.sql.Timestamp;

@Entity(value = "save_data", useDiscriminator = false)
@Indexes({
    @Index(fields = {@Field("user"), @Field("profile")}),
    @Index(fields = @Field(value = "contents", type = IndexType.TEXT))
})
public class MongoSaveDataDocument {

    @Id
    private MongoSaveDataDocumentId saveDataDocumentId;

    @Property
    private byte[] version;

    @Property
    private String contents;

    @Property
    private Timestamp timestamp;

    @Property
    private String digestAlgorithm;

    @Reference
    private MongoUser user;

    @Reference
    private MongoProfile profile;

    public MongoSaveDataDocumentId getSaveDataDocumentId() {
        return saveDataDocumentId;
    }

    public void setSaveDataDocumentId(MongoSaveDataDocumentId saveDataDocumentId) {
        this.saveDataDocumentId = saveDataDocumentId;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public byte[] getVersion() {
        return version;
    }

    public void setVersion(byte[] version) {
        this.version = version;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }

    public MongoProfile getProfile() {
        return profile;
    }

    public void setProfile(MongoProfile profile) {
        this.profile = profile;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

}
