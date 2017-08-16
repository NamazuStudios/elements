package com.namazustudios.socialengine.rt.manifest.model;

/**
 * Created by patricktwohig on 8/16/17.
 */
public class Property {

    private String name;

    private Type type;

    private String model;

    /**
     * Gets the name of this property.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Property.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The type of this property.
     *
     * @return the type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of this property.
     *
     * @param type the type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets the model name, if this is a complex type. (eg {@link Type#ARRAY} or {@link Type#OBJECT})
     * such that it may property reference another model in the manifest.  THis corresponds to
     * the values of {@link Model#getName()} and the key in {@link ModelManifest#getModelsByName()}.
     *
     * @return
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the model name.
     *
     * @param model the model name.
     */
    public void setModel(String model) {
        this.model = model;
    }

}
