package dev.getelements.elements.dao.mongo.model.system;

import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.dao.mongo.model.largeobject.MongoLargeObject;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import dev.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.util.List;

@Entity(value = "element_deployment", useDiscriminator = false)
public class MongoElementDeployment {

    @Id
    private ObjectId objectId;

    @Indexed
    @Reference
    private MongoApplication application;

    @Property
    private List<String> apiArtifacts;

    @Property
    private List<String> spiArtifacts;

    @Property
    private List<String> elementArtifacts;

    @Reference
    private MongoLargeObject elm;

    @Property
    private String elmArtifact;

    @Property
    private boolean useDefaultRepositories;

    @Property
    private List<ArtifactRepository> repositories;

    @Property
    private ElementDeploymentState state;

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

    public MongoLargeObject getElm() {
        return elm;
    }

    public void setElm(MongoLargeObject elm) {
        this.elm = elm;
    }

    public String getElmArtifact() {
        return elmArtifact;
    }

    public void setElmArtifact(String elmArtifact) {
        this.elmArtifact = elmArtifact;
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

    public ElementDeploymentState getState() {
        return state;
    }

    public void setState(ElementDeploymentState state) {
        this.state = state;
    }

}
