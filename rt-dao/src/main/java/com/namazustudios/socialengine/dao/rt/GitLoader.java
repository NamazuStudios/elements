package com.namazustudios.socialengine.dao.rt;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Path;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by patricktwohig on 8/22/17.
 */
public interface GitLoader {

    /**
     * The main branch from which to fetch the code.  The underlying {@link GitLoader} will
     * ignore all other branches, except this one, when fetching code.  It should be noted
     * that this may become obsolte if application configuration allows for specific
     * branches or commits to be used as the reference for the {@link Application} that is
     * associated with the code.
     */
    String DEFAULT_MAIN_BRANCH = "master";

    /**
     * The git directory suffix.
     */
    String GIT_SUFFIX = "git";

    /**
     * Opens a {@link Git} instance for the supplied {@link Application} which can be used to manipulate the various
     * files within the repository.  When call returns the supplied {@link Git} instance will be closed.
     *
     * @param  application the {@link Application} for which to issueOpenInprocChannelCommand a {@link Git} instance
     * @param gitConsumer consumes an instance of {@link Git} which will be used to perform the desired actions
     */
    void performInGit(final Application application, final BiConsumer<Git, Function<Path, OutputStream>> gitConsumer);

    /**
     * Gets the code directory for the supplied {@Link Application} and clones if necessary.  This will
     * ensure that the latest branch, as specified by {@link #DEFAULT_MAIN_BRANCH} is checked out and current.
     *
     * The returned {@link File} will likely be a temporary live copy of the code backing the {@link Application}.
     *
     * @param application the {@link Application} instance
     *
     * @return the {@link }
     */
    File getCodeDirectory(Application application);

}
