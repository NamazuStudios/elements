package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.rt.annotation.ClientSerializationStrategy;

import static com.namazustudios.socialengine.rt.annotation.ClientSerializationStrategy.APPLE_ITUNES;

@ClientSerializationStrategy(APPLE_ITUNES)
public class TestSnakeModel {

    private String testProperty;

    public String getTestProperty() {
        return testProperty;
    }

    public void setTestProperty(String testProperty) {
        this.testProperty = testProperty;
    }

}
