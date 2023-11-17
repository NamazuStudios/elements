package dev.getelements.elements.dao.mongo.model.index;

import dev.morphia.annotations.*;

import java.sql.Timestamp;

@Entity(value = "index_operation")
@Indexes({
        @Index(fields = @Field(value = "expiry"), options = @IndexOptions(expireAfterSeconds = 0)),
})
public class MongoIndexOperation {

    @Id
    private String id;

    @Property
    private Timestamp expiry;

    @Property
    private String uuid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
