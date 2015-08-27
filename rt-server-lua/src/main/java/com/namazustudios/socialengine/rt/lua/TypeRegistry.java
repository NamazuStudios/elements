package com.namazustudios.socialengine.rt.lua;

import com.namazustudios.socialengine.exception.NotFoundException;

/**
 * Various scripts in the server may need to access type metadata through various means.  This provides
 * an interface through which types can be accessed by simple names to avoid having to references
 * types by fully-qualified Java class name.
 *
 * Created by patricktwohig on 8/26/15.
 */
public interface TypeRegistry {

    /**
     * Gets the event type with the given name.
     *
     * @param name the name of the event
     * @return the type
     *
     * @throws {@link NotFoundException} if the type cannot be found
     */
    Class<?> getEventTypeNamed(final String name);

    /**
     * Gets the request type with the given name.
     *
     * @param name the name of the event
     * @return the type
     *
     * @throws {@link NotFoundException} if the type cannot be found
     */
    Class<?> getRequestTypeNamed(final String name);

    /**
     * Gets the request type with the given name.
     *
     * @param name the name of the event
     * @return the type
     *
     * @throws {@link NotFoundException} if the type cannot be found
     */
    Class<?> getResponseTypeNamed(final String name);

    /**
     * Gets the types by fully-qualified class name.
     *
     * @param classFqn the fully-qualified classs name
     * @return the type
     *
     * @throws {@link NotFoundException} if the type cannot be found
     */
    Class<?> getFullyQualified(final String classFqn);

}
