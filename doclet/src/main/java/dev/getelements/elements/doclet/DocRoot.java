package dev.getelements.elements.doclet;

import dev.getelements.elements.rt.annotation.Private;

import java.util.List;

/**
 * Represents a documentation root resource.
 */
@Private
public interface DocRoot {

    /**
     * Gets the name of the documentation root.
     *
     * @return the name
     */
    List<String> getRelativePath();

    /**
     * Writes this {@link DocRoot} using the supplied {@link DocRootWriter}
     * @param writer
     */
    void write(DocRootWriter writer);

}
