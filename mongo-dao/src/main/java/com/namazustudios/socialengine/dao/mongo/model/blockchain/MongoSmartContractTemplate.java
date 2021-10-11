package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import com.namazustudios.socialengine.model.application.Application;
import dev.morphia.annotations.*;
import dev.morphia.utils.IndexType;
import org.bson.types.ObjectId;

import java.util.List;

@SearchableIdentity(@SearchableField(
        name = "id",
        path = "/objectId",
        type = ObjectId.class,
        extractor = ObjectIdExtractor.class,
        processors = ObjectIdProcessor.class))
@Entity(value = "profile", useDiscriminator = false)
@Indexes({
        @Index(fields = {@Field("user")}),
        @Index(fields = @Field(value = "displayName", type = IndexType.TEXT))
})
public class MongoSmartContractTemplate {

    @Id
    public String id;

    @Indexed
    @Property
    public String name;

    @Property
    public Object contractBinary;

    @Reference
    private Application application;

    private List<String> tags;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getContractBinary() {
        return contractBinary;
    }

    public void setContractBinary(Object contractBinary) {
        this.contractBinary = contractBinary;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
