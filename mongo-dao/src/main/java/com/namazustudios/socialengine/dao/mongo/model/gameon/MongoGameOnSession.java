package com.namazustudios.socialengine.dao.mongo.model.gameon;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import com.namazustudios.socialengine.model.gameon.game.AppBuildType;
import org.bson.types.ObjectId;
import dev.morphia.annotations.*;

import java.sql.Timestamp;
import java.util.Objects;

@SearchableIdentity(@SearchableField(
    name = "id",
    path = "/objectId",
    type = ObjectId.class,
    extractor = MongoGameOnSessionId.Extractor.class,
    processors = MongoGameOnSessionId.Processor.class))
@SearchableDocument(
    fields = {
        @SearchableField(name = "userId",      path = "/user/objectId", extractor = ObjectIdExtractor.class, processors = ObjectIdProcessor.class),
        @SearchableField(name = "profileId",   path = "/profile/objectId", extractor = ObjectIdExtractor.class, processors = ObjectIdProcessor.class),
        @SearchableField(name = "userName",    path = "/user/name"),
        @SearchableField(name = "userEmail",   path = "/user/email"),
        @SearchableField(name = "displayName", path = "/profile/displayName")
    })
@Entity(value = "game_on_session", noClassnameStored = true)
@Indexes({
    @Index(fields = @Field("_id.deviceOSType"))
})
public class MongoGameOnSession {

    @Id
    private MongoGameOnSessionId objectId;

    @Indexed
    @Reference
    private MongoUser user;

    @Indexed
    @Reference
    private MongoProfile profile;

    @Indexed(options = @IndexOptions(expireAfterSeconds = 0))
    private Timestamp sessionExpirationDate;

    @Property
    private String sessionId;

    @Property
    private String sessionApiKey;

    @Property
    private AppBuildType appBuildType;

    public MongoGameOnSessionId getObjectId() {
        return objectId;
    }

    public void setObjectId(MongoGameOnSessionId objectId) {
        this.objectId = objectId;
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

    public Timestamp getSessionExpirationDate() {
        return sessionExpirationDate;
    }

    public void setSessionExpirationDate(Timestamp sessionExpirationDate) {
        this.sessionExpirationDate = sessionExpirationDate;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionApiKey() {
        return sessionApiKey;
    }

    public void setSessionApiKey(String sessionApiKey) {
        this.sessionApiKey = sessionApiKey;
    }

    public AppBuildType getAppBuildType() {
        return appBuildType;
    }

    public void setAppBuildType(AppBuildType appBuildType) {
        this.appBuildType = appBuildType;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof MongoGameOnSession)) return false;
        MongoGameOnSession that = (MongoGameOnSession) object;
        return Objects.equals(getObjectId(), that.getObjectId()) &&
                Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getProfile(), that.getProfile()) &&
                Objects.equals(getSessionExpirationDate(), that.getSessionExpirationDate()) &&
                Objects.equals(getSessionId(), that.getSessionId()) &&
                Objects.equals(getSessionApiKey(), that.getSessionApiKey()) &&
                getAppBuildType() == that.getAppBuildType();
    }

    @Override
    public int hashCode() {

        return Objects.hash(getObjectId(), getUser(), getProfile(), getSessionExpirationDate(), getSessionId(), getSessionApiKey(), getAppBuildType());
    }

    @Override
    public String toString() {
        return "MongoGameOnSession{" +
                "objectId=" + objectId +
                ", user=" + user +
                ", profile=" + profile +
                ", sessionExpirationDate=" + sessionExpirationDate +
                ", sessionId='" + sessionId + '\'' +
                ", sessionApiKey='" + sessionApiKey + '\'' +
                ", appBuildType=" + appBuildType +
                '}';
    }

}
