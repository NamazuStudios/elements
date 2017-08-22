package com.namazustudios.socialengine.dao.rt;

import com.namazustudios.socialengine.model.application.Application;

import java.io.File;

/**
 * Created by patricktwohig on 8/22/17.
 */
public interface GitLoader {

    /**
     * The main branch from which to fetch the code.
     */
    String MAIN_BRANCH = "master";

    /**
     * The git directory suffix.
     */
    String GIT_SUFFIX = "git";

    /**
     * Gets the code directory for the supplied {@Link Application} and clones if necessary.  This will
     * ensure that the latest branch, as specified by {@link #MAIN_BRANCH} is checked out and current.
     *
     * @param application the {@link Application} instance
     *
     * @return the {@link }
     */
    File getCodeDirectory(Application application);

}
