package dev.getelements.elements.rt;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents a set of mutable attributes.
 */
public interface MutableAttributes extends Attributes {

    /**
     * Sets the attribute with the supplied name and value
     * @param name the name to set
     * @param obj the value to set
     */
    void setAttribute(String name, Object obj);

}
