package dev.getelements.elements.dao.mongo.model.schema;

import dev.getelements.elements.dao.mongo.model.blockchain.MongoTemplateTab;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;


@Entity(value = "metadata_spec", useDiscriminator = false)
public class MongoMetadataSpec {

    @Id
    private ObjectId objectId;

    @Property
    @Indexed(options = @IndexOptions(unique = true))
    private String name;

    @Property
    private List<MongoTemplateTab> tabs;

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

    public List<MongoTemplateTab>  getTabs() {
        return tabs;
    }

    public void setTabs(List<MongoTemplateTab> tabs) {
        this.tabs = tabs;
    }
}
