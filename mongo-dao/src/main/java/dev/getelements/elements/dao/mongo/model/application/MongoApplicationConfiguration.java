package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;

/**
 * Created by patricktwohig on 7/10/15.
 */
@Indexes({
        @Index(
                fields = @Field("name")
        ),
        @Index(
                fields = @Field("parent")
        ),
        @Index(
                fields = {
                        @Field("_t"),
                        @Field("parent"),
                        @Field("name")
                },
                options = @IndexOptions(unique = true, sparse = true)
        ),
        @Index(fields = @Field(value = "description", type = IndexType.TEXT))
})
@Entity(value = "application_configuration")
public class MongoApplicationConfiguration {

    @Id
    private ObjectId objectId;

    @Property
    private String name;

    @Property
    private String description;

    @Reference
    private MongoApplication parent;

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

    public MongoApplication getParent() {
        return parent;
    }

    public void setParent(MongoApplication parent) {
        this.parent = parent;
    }

}
