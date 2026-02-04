package dev.getelements.elements.dao.mongo.model.system;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import java.util.List;

@Embedded
public class MongoElementDefinition {

    @Property
    private List<String> apiArtifacts;

    @Property
    private List<String> spiArtifacts;

    @Property
    private List<String> elementArtifacts;

    @Property
    private String elmArtifact;

    public List<String> getApiArtifacts() {
        return apiArtifacts;
    }

    public void setApiArtifacts(List<String> apiArtifacts) {
        this.apiArtifacts = apiArtifacts;
    }

    public List<String> getSpiArtifacts() {
        return spiArtifacts;
    }

    public void setSpiArtifacts(List<String> spiArtifacts) {
        this.spiArtifacts = spiArtifacts;
    }

    public List<String> getElementArtifacts() {
        return elementArtifacts;
    }

    public void setElementArtifacts(List<String> elementArtifacts) {
        this.elementArtifacts = elementArtifacts;
    }

    public String getElmArtifact() {
        return elmArtifact;
    }

    public void setElmArtifact(String elmArtifact) {
        this.elmArtifact = elmArtifact;
    }

}
