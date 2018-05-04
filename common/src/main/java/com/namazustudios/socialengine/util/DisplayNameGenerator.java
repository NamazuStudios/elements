package com.namazustudios.socialengine.util;

import com.namazustudios.socialengine.model.profile.Profile;

/**
 * Generates a fake user Display name.  For example, for use with {@link Profile#getDisplayName()} when creating fake
 * or test accounts.
 */
public interface DisplayNameGenerator {

    /**
     * Generates a random display name.
     *
     * @return the display name.
     */
    String generate();

}
