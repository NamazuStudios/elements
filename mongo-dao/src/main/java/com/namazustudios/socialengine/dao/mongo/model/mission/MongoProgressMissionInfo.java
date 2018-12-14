package com.namazustudios.socialengine.dao.mongo.model.mission;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.Property;

import java.util.List;

@Embedded
public class MongoProgressMissionInfo {

    @Indexed
    @Property
    private String name;

    @Property
    private String displayName;

    @Property
    private String description;

    @Embedded
    private List<MongoStep> steps;

    @Embedded
    private MongoStep finalRepeatStep;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MongoStep> getSteps() {
        return steps;
    }

    public void setSteps(List<MongoStep> steps) {
        this.steps = steps;
    }

    public MongoStep getFinalRepeatStep() {
        return finalRepeatStep;
    }

    public void setFinalRepeatStep(MongoStep finalRepeatStep) {
        this.finalRepeatStep = finalRepeatStep;
    }


}
