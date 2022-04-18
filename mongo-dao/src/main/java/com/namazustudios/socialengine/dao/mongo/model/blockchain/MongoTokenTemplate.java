package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
import com.namazustudios.socialengine.model.ValidationGroups;
import dev.morphia.annotations.*;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

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
    private String tokenName;

    @Property
    private String contractId;

    @Property
    private List<MongoTemplateTab> tabs;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public List<MongoTemplateTab>  getTabs() {
        return tabs;
    }

    public void setTabs(List<MongoTemplateTab> tabs) {
        this.tabs = tabs;
    }
}
