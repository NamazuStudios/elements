package dev.getelements.elements.dao.mongo.model.goods;

import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

@Entity(value = "product_sku_schema", useDiscriminator = false)
@Indexes({
        @Index(fields = @Field("schema"), options = @IndexOptions(unique = true))
})
public class MongoProductSkuSchema {

    @Id
    private ObjectId objectId;

    @Property
    private String schema;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

}
