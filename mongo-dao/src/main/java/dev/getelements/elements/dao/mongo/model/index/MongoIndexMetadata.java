package dev.getelements.elements.dao.mongo.model.index;

import dev.getelements.elements.dao.index.IndexMetadata;
import dev.morphia.annotations.Property;
import org.bson.Document;

import java.util.Objects;

public class MongoIndexMetadata implements IndexMetadata<Document> {

    @Property
    private Document keys;

    @Property
    private Document options;

    @Property
    private Document commitQuorum;

    @Override
    public Document getIdentifier() {
        return null;
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
        return Objects.equals(getKeys(), that.getKeys()) && Objects.equals(options, that.options) && Objects.equals(commitQuorum, that.commitQuorum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKeys(), options, commitQuorum);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoIndexMetadata{");
        sb.append("keys=").append(keys);
        sb.append(", options=").append(options);
        sb.append(", commitQuorum=").append(commitQuorum);
        sb.append('}');
        return sb.toString();
    }

}
