package com.namazustudios.socialengine.fts;

import org.apache.commons.jxpath.JXPathContext;

/**
 * Used to specify a custom instance of {@link JXPathContext}.
 *
 * Created by patricktwohig on 5/31/15.
 */
public interface JXPathContextProvider {

    /**
     * The root object must have a variable assigned to itself, with
     * this name.
     */
    String ROOT_OBJECT_NAME = "document";

    /**
     * Provides a JXPathContext to the {@link DocumentGenerator}.
     *
     * @param rootObject the object to use as the orot
     * @return
     */
    JXPathContext get(final Object rootObject);

}
