package dev.getelements.elements.rt.lua.persist;

import dev.getelements.elements.rt.Attributes;
import dev.getelements.elements.rt.id.ResourceId;

import java.io.Serializable;
import java.util.List;

public class SerialHeader implements Serializable {

    private ResourceId resourceId;

    private Attributes attributes;

    private List<Object> objectTable;

    public ResourceId getResourceId() {
        return resourceId;
    }

    public void setResourceId(ResourceId resourceId) {
        this.resourceId = resourceId;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public List<Object> getObjectTable() {
        return objectTable;
    }

    public void setObjectTable(List<Object> objectTable) {
        this.objectTable = objectTable;
    }

}
