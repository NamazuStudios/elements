package dev.getelements.elements.dao.mongo.model.system;

import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Property;

import java.util.List;
import java.util.Map;

@Embedded
public class MongoElementPackageDefinition {

    @Property
    private String elmArtifact;

    @Property
    private Map<String, List<String>> pathSpiClassPaths;

    @Property
    private Map<String, Map<String, Object>> pathAttributes;

    public String getElmArtifact() {
        return elmArtifact;
    }

    public void setElmArtifact(String elmArtifact) {
        this.elmArtifact = elmArtifact;
    }

    public Map<String, List<String>> getPathSpiClassPaths() {
        return pathSpiClassPaths;
    }

    public void setPathSpiClassPaths(Map<String, List<String>> pathSpiClassPaths) {
        this.pathSpiClassPaths = pathSpiClassPaths;
    }

    public Map<String, Map<String, Object>> getPathAttributes() {
        return pathAttributes;
    }

    public void setPathAttributes(Map<String, Map<String, Object>> pathAttributes) {
        this.pathAttributes = pathAttributes;
    }

}