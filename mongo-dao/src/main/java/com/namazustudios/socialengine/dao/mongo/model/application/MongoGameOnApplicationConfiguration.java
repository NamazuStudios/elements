package com.namazustudios.socialengine.dao.mongo.model.application;

import com.namazustudios.elements.fts.annotation.SearchableDocument;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Property;

@SearchableDocument
@Entity(value = "application_configuration", noClassnameStored = true)
public class MongoGameOnApplicationConfiguration extends MongoApplicationConfiguration {

    @Property
    private String publicApiKey;

    @Property
    private String adminApiKey;

    @Property
    private String publicKey;

    public String getPublicApiKey() {
        return publicApiKey;
    }

    public void setPublicApiKey(String publicApiKey) {
        this.publicApiKey = publicApiKey;
    }

    public String getAdminApiKey() {
        return adminApiKey;
    }

    public void setAdminApiKey(String adminApiKey) {
        this.adminApiKey = adminApiKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

}
