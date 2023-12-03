package dev.getelements.elements.dao.mongo.model.application;

import dev.morphia.annotations.Entity;

import java.util.ArrayList;
import java.util.List;

@Entity(value = "application_configuration", useDiscriminator = false)
public class MongoGoogleSignInApplicationConfiguration extends MongoApplicationConfiguration {

    private ArrayList<String> client_ids;

    public ArrayList<String> getClient_ids() {
        return client_ids;
    }

    public void setClient_ids(ArrayList<String> client_ids) {
        this.client_ids = client_ids;
    }
}
