package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.rt.annotation.ClientSerializationStrategy;

import static com.namazustudios.socialengine.rt.annotation.ClientSerializationStrategy.LCAMEL;

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
