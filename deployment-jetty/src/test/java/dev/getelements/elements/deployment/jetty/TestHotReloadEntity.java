package dev.getelements.elements.deployment.jetty;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

/**
 * A stand-in element-owned {@code @Entity} used by {@link StandardElementRuntimeServiceReloadTest} to drive
 * the pre-load entity registration (#209). Loaded through a dedicated classloader so the registrar's scan
 * sees exactly this class.
 */
@Entity
public class TestHotReloadEntity {

    @Id
    private String id;

    private String value;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

}
