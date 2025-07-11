package dev.getelements.elements.dao.mongo.model;

import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.sql.Timestamp;

@Entity(value = "deployment")
@Indexes(
    @Index(
        fields = { @Field("application"), @Field("version") },
        options = @IndexOptions(unique = true)
    )
)
public class MongoDeployment {

    @Id
    private ObjectId objectId;

    @Property
    private String version;

    @Property
    private String revision;

    @Property
    private Timestamp createdAt;

    @Indexed
    @Reference
    private MongoApplication application;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public MongoApplication getApplication() {
        return application;
    }

    public void setApplication(MongoApplication application) {
        this.application = application;
    }

}
