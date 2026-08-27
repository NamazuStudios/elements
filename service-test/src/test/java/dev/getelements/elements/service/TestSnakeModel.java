package dev.getelements.elements.service;

import dev.getelements.elements.sdk.model.annotation.ClientSerializationStrategy;

import static dev.getelements.elements.sdk.model.annotation.ClientSerializationStrategy.APPLE_ITUNES;

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
