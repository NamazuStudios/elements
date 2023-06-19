package dev.getelements.elements.rt.git;

import dev.getelements.elements.rt.Path;
import dev.getelements.elements.rt.id.ApplicationId;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.OutputStream;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The GitLoader takes a clone of an existing git repository, and allows downstream code access to that directory. In
 * addition to the clone process, this provides a {@link File} indicating the root of the cloned directory. When
 * loading, this will ensure the local copy of the code is available to the downstream code.
 *
 * Created by patricktwohig on 8/22/17.
 */
public interface GitLoader {

    /**
     * The main branch from which to fetch the code.  The underlying {@link GitLoader} will
     * ignore all other branches, except this one, when fetching code.  It should be noted
     * that this may become obsolte if application configuration allows for specific
     * branches or commits to be used as the reference for the {@link ApplicationId} that is
     * associated with the code.
     */
    String DEFAULT_MAIN_BRANCH = "master";

    /**
     * The git directory suffix.
     */
    String GIT_SUFFIX = "git";

    /**
     * Opens a {@link Git} instance for the supplied {@link ApplicationId} which can be used to manipulate the various
     * files within the repository.  When call returns the supplied {@link Git} instance will be closed.
     *
     * {@see {@link ApplicationId#forUniqueName(String)}} to understand how this method interprets the first parameter.
     *
     * @param applicationUniqueName the {@link ApplicationId} for which to open a {@link Git} instance
     * @param gitConsumer consumes an instance of {@link Git} which will be used to perform the desired actions
     */
    default void performInGit(final String applicationUniqueName,
                              final BiConsumer<Git, Function<Path, OutputStream>> gitConsumer) {
        final var id = ApplicationId.forUniqueName(applicationUniqueName);
        performInGit(id, gitConsumer);
    }

    /**
     * Opens a {@link Git} instance for the supplied {@link ApplicationId} which can be used to manipulate the various
     * files within the repository.  When call returns the supplied {@link Git} instance will be closed.
     *
     * @param applicationId the {@link ApplicationId} for which to open a {@link Git} instance
     * @param gitConsumer consumes an instance of {@link Git} which will be used to perform the desired actions
     */
    void performInGit(ApplicationId applicationId, BiConsumer<Git, Function<Path, OutputStream>> gitConsumer);

    /**
     * Gets the code directory for the supplied {@link ApplicationId} and clones if necessary.  This will
     * ensure that the latest branch, as specified by {@link #DEFAULT_MAIN_BRANCH} is checked out and current.
     *
     * The returned {@link File} will likely be a temporary live copy of the code backing the {@link ApplicationId}.
     *
     * @param applicationId the {@link ApplicationId} instance
     *
     * @return the {@link File} to the git repository
     */
    File getCodeDirectory(ApplicationId applicationId);

}
