package dev.getelements.elements.dao.mongo.model.system;

import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.dao.mongo.model.largeobject.MongoLargeObject;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@Entity(value = "element_deployment", useDiscriminator = false)
public class MongoElementDeployment {

    @Id
    private ObjectId objectId;

    @Indexed
    @Reference
    private MongoApplication application;

    @Reference(ignoreMissing = true)
    private MongoLargeObject elm;

    @Property
    private List<MongoElementPathDefinition> elements;

    @Property
    private List<MongoElementPackageDefinition> packages;

    @Property
    private boolean useDefaultRepositories;

    @Property
    private List<ArtifactRepository> repositories;

    @Property
    private Map<String, List<String>> pathSpiBuiltins;

    @Property
    private Map<String, List<String>> pathSpiClassPaths;

    @Property
    private Map<String, Map<String, Object>> pathAttributes;

    @Property
    private ElementDeploymentState state;

    @Property
    private long version;

    public ObjectId getObjectId() {
        return objectId;
    }

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
    }

    public MongoApplication getApplication() {
        return application;
    }

    public void setApplication(MongoApplication application) {
        this.application = application;
    }

    public MongoLargeObject getElm() {
        return elm;
    }

    public void setElm(MongoLargeObject elm) {
        this.elm = elm;
    }

    public List<MongoElementPathDefinition> getElements() {
        return elements;
    }

    public void setElements(List<MongoElementPathDefinition> elements) {
        this.elements = elements;
    }

    public List<MongoElementPackageDefinition> getPackages() {
        return packages;
    }

    public void setPackages(List<MongoElementPackageDefinition> packages) {
        this.packages = packages;
    }

    public boolean isUseDefaultRepositories() {
        return useDefaultRepositories;
    }

    public void setUseDefaultRepositories(boolean useDefaultRepositories) {
        this.useDefaultRepositories = useDefaultRepositories;
    }

    public List<ArtifactRepository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<ArtifactRepository> repositories) {
        this.repositories = repositories;
    }

    public Map<String, List<String>> getPathSpiBuiltins() {
        return pathSpiBuiltins;
    }

    public void setPathSpiBuiltins(Map<String, List<String>> pathSpiBuiltins) {
        this.pathSpiBuiltins = pathSpiBuiltins;
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

    public ElementDeploymentState getState() {
        return state;
    }

    public void setState(ElementDeploymentState state) {
        this.state = state;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}
