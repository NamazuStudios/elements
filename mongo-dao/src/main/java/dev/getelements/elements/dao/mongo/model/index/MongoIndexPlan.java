package dev.getelements.elements.dao.mongo.model.index;

import dev.morphia.annotations.*;

import java.util.List;

@Entity(value = "index_plan")
public class MongoIndexPlan {

    @Id
    private String id;

    @Property
    private List<MongoIndexPlanStep> steps;

    @Property
    private List<MongoIndexMetadata> existing;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<MongoIndexPlanStep> getSteps() {
        return steps;
    }

    public void setSteps(List<MongoIndexPlanStep> steps) {
        this.steps = steps;
    }

    public List<MongoIndexMetadata> getExisting() {
        return existing;
    }

    public void setExisting(List<MongoIndexMetadata> existing) {
        this.existing = existing;
    }

}
