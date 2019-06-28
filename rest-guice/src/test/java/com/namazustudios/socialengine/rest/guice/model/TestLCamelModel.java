package com.namazustudios.socialengine.rest.guice.model;

import com.namazustudios.socialengine.annotation.ClientSerializationStrategy;

import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.LCAMEL;

@ClientSerializationStrategy(LCAMEL)
public class TestLCamelModel {

    private String testProperty;

    public String getTestProperty() {
        return testProperty;
    }

    public void setTestProperty(String testProperty) {
        this.testProperty = testProperty;
    }

}
