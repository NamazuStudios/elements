package dev.getelements.elements.dao.mongo.model.schema.email;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.Date;

@Entity(value = "email_template", useDiscriminator = false)
public class MongoEmailTemplate {

    @Id
    private ObjectId objectId;

    @Property
    @Indexed(options = @IndexOptions(unique = true, sparse = true))
    private String key;

    @Property
    private String name;

    @Property
    private String subject;

    @Property
    private String body;

    @Property
    private String description;

    @Property
    private Date createdAt;

    @Property
    private Date updatedAt;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

}
