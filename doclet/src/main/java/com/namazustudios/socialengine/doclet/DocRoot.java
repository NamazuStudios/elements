package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.rt.annotation.Private;

/**
 * Represents a documentation root resource.
 */
@Private
public interface DocRoot {

    /**
     * Writes this {@link DocRoot} using the supplied {@link DocRootWriter}
     * @param writer
     */
    void write(DocRootWriter writer);

}
