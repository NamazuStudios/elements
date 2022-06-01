package com.namazustudios.socialengine.dao.mongo.model.schema;

import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoNeoSmartContract;
import com.namazustudios.socialengine.dao.mongo.model.blockchain.MongoTemplateTab;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.blockchain.ElementsSmartContract;
import com.namazustudios.socialengine.model.schema.template.MetadataSpec;
import dev.morphia.annotations.*;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.osgi.service.useradmin.User;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Map;

@SearchableIdentity(@SearchableField(
        name = "objectId",
        path = "/objectId",
        type = ObjectId.class,
        extractor = ObjectIdExtractor.class,
        processors = ObjectIdProcessor.class))
@Entity(value = "token_template", useDiscriminator = false)
public class MongoTokenTemplate {

    @Id
    private ObjectId objectId;

    @Property
    @Indexed(options = @IndexOptions(unique = true))
    private String name;

    @Property
    private String displayName;

    @Reference
    MongoMetadataSpec metadataSpec;

    @Property
    Map<String, Object> metadata;

    @Reference
    public MongoUser user;

    @Reference
    MongoNeoSmartContract contract;

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public MongoMetadataSpec getMetadataSpec() {
        return metadataSpec;
    }

    public void setMetadataSpec(MongoMetadataSpec metadataSpec) {
        this.metadataSpec = metadataSpec;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public MongoNeoSmartContract getContract() {
        return contract;
    }

    public void setContract(MongoNeoSmartContract contract) {
        this.contract = contract;
    }

    public MongoUser getUser() {
        return user;
    }

    public void setUser(MongoUser user) {
        this.user = user;
    }
}
