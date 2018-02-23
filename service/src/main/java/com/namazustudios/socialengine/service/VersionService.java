package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Version;

/**
 * Gets the {@link Version} of the current build.
 *
 */
public interface VersionService {

    /**
     * Returns the current {@link Version} of the current built.
     *
     * @return the {@link Version}
     */
    Version getVersion();

}
