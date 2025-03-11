package dev.getelements.elements.service;

import dev.getelements.elements.rt.annotation.ClientSerializationStrategy;

import static dev.getelements.elements.rt.annotation.ClientSerializationStrategy.LCAMEL;

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
