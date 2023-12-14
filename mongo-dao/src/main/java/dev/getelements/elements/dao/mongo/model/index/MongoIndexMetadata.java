package dev.getelements.elements.dao.mongo.model.index;

import dev.getelements.elements.model.index.IndexMetadata;
import dev.morphia.annotations.Property;
import org.bson.Document;

import java.util.Objects;

public class MongoIndexMetadata implements IndexMetadata<Document> {

    @Property
    private Document keys;

    @Override
    public Document getIdentifier() {
        return getKeys();
    }

    public Document getKeys() {
        return keys;
    }

    public void setKeys(Document keys) {
        this.keys = keys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MongoIndexMetadata that = (MongoIndexMetadata) o;
        return Objects.equals(getKeys(), that.getKeys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKeys());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoIndexMetadata{");
        sb.append("keys=").append(keys);
        sb.append('}');
        return sb.toString();
    }

}
