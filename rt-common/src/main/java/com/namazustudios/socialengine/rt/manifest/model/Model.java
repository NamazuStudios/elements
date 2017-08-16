package com.namazustudios.socialengine.rt.manifest.model;

import java.util.Map;

/**
 * Created by patricktwohig on 8/16/17.
 */
public class Model {

    private String name;

    private Map<String, Property> properties;

    /**
     * Get the name of the model.
     *
     * @return the name of the model
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the model.
     *
     * @param name the name of the model
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the mapping of properties.
     *
     * @return the {@link Map<String, Property>}
     */
    public Map<String, Property> getProperties() {
        return properties;
    }

    /**
     * Sets the mapping of properties.
     *
     * @param properties the {@link Map<String, Property>}
     */
    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }

}
