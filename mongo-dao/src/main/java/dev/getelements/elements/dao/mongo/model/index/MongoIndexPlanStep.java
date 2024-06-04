package dev.getelements.elements.dao.mongo.model.index;

import dev.getelements.elements.model.index.IndexOperation;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

@Entity
public class MongoIndexPlanStep {

    @Property
    private String description;

    @Property
    private IndexOperation operation;

    @Property
    private MongoIndexMetadata indexMetadata;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IndexOperation getOperation() {
        return operation;
    }

    public void setOperation(IndexOperation operation) {
        this.operation = operation;
    }

    public MongoIndexMetadata getIndexMetadata() {
        return indexMetadata;
    }

    public void setIndexMetadata(MongoIndexMetadata indexMetadata) {
        this.indexMetadata = indexMetadata;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MongoIndexPlanStep{");
        sb.append("description='").append(description).append('\'');
        sb.append(", operation=").append(operation);
        sb.append(", indexMetadata=").append(indexMetadata);
        sb.append('}');
        return sb.toString();
    }

}
