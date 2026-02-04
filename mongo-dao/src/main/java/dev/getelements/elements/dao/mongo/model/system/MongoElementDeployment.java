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

    @Reference
    private MongoLargeObject elm;

    @Property
    private List<MongoElementDefinition> elements;

    @Property
    private boolean useDefaultRepositories;

    @Property
    private List<ArtifactRepository> repositories;

    @Property
    private Map<String, Object> attributes;

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

    public List<MongoElementDefinition> getElements() {
        return elements;
    }

    public void setElements(List<MongoElementDefinition> elements) {
        this.elements = elements;
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

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
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
