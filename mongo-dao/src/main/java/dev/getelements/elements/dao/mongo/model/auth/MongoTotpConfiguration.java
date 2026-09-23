package dev.getelements.elements.dao.mongo.model.auth;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import org.bson.types.ObjectId;

import java.util.Objects;

@Entity(value = "totp_configuration", useDiscriminator = false)
public class MongoTotpConfiguration {

    @Id
    private ObjectId id;

    @Property
    private boolean enabled;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoTotpConfiguration that = (MongoTotpConfiguration) o;
        return enabled == that.enabled && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, enabled);
    }

}
