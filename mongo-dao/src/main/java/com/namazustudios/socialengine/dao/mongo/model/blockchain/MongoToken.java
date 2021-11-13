package com.namazustudios.socialengine.dao.mongo.model.blockchain;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import com.namazustudios.elements.fts.annotation.SearchableField;
import com.namazustudios.elements.fts.annotation.SearchableIdentity;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdExtractor;
import com.namazustudios.socialengine.dao.mongo.model.ObjectIdProcessor;
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
@Entity(value = "token", useDiscriminator = false)
@SearchableDocument(fields = {
        @SearchableField(name = "name", path = "/name"),
        @SearchableField(name = "tags", path = "/tags"),
        @SearchableField(name = "type", path = "/type")
})
@Indexes({
        @Index(fields = @Field(value = "name", type = IndexType.TEXT))
})
public class MongoToken {

    @Id
    public String id;

    @Property
    public String name;

    @Property
    public String description;

    @Property
    public String type;

    @Property
    public List<String> tags;

    @Property
    public int royaltyPercentage;

    @Property
    public long rentDuration;

    @Property
    public long quantity;

    @Property
    public String transferOptions;

    @Property
    public boolean publiclyAccessible;

    @Property
    public String previewUrl;

    @Property
    public List<String> assetUrls;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getRoyaltyPercentage() {
        return royaltyPercentage;
    }

    public void setRoyaltyPercentage(int royaltyPercentage) {
        this.royaltyPercentage = royaltyPercentage;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getTransferOptions() {
        return transferOptions;
    }

    public void setTransferOptions(String transferOptions) {
        this.transferOptions = transferOptions;
    }

    public boolean isPubliclyAccessible() {
        return publiclyAccessible;
    }

    public void setPubliclyAccessible(boolean publiclyAccessible) {
        this.publiclyAccessible = publiclyAccessible;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public List<String> getAssetUrls() {
        return assetUrls;
    }

    public void setAssetUrls(List<String> assetUrls) {
        this.assetUrls = assetUrls;
    }

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
}
