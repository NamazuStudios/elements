package dev.getelements.elements.dao.mongo.model.schema;

import dev.getelements.elements.sdk.model.schema.MetadataSpecPropertyType;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;


@Entity(value = "metadata_spec", useDiscriminator = false)
public class MongoMetadataSpec {

    @Id
    private ObjectId objectId;

    @Property
    @Indexed(options = @IndexOptions(unique = true, sparse = true))
    private String name;

    @Property
    private MetadataSpecPropertyType type;

    @Property
    private List<MongoMetadataSpecProperty> properties;

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

    public MetadataSpecPropertyType getType() {
        return type;
    }

    public void setType(MetadataSpecPropertyType type) {
        this.type = type;
    }

    public List<MongoMetadataSpecProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<MongoMetadataSpecProperty> properties) {
        this.properties = properties;
    }

}
