package dev.getelements.elements.service;

import dev.getelements.elements.rt.annotation.ClientSerializationStrategy;

import static dev.getelements.elements.rt.annotation.ClientSerializationStrategy.APPLE_ITUNES;

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
