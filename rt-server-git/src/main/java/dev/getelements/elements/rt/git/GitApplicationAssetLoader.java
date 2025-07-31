package dev.getelements.elements.rt.git;

import dev.getelements.elements.sdk.cluster.ApplicationAssetLoader;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import org.eclipse.jgit.api.Git;

import java.nio.file.Path;
import java.util.function.BiConsumer;

/**
 * The GitLoader takes a clone of an existing git repository, and allows downstream code access to that directory.
 *
 * Created by patricktwohig on 8/22/17.
 */
public interface GitApplicationAssetLoader extends ApplicationAssetLoader {

    /**
     * The git directory suffix.
     */
    String GIT_SUFFIX = "git";

    /**
     * The main branch from which to fetch the code.  The underlying {@link GitApplicationAssetLoader} will
     * ignore all other branches, except this one, when fetching code.  It should be noted
     * that this may become obsolte if application configuration allows for specific
     * branches or commits to be used as the reference for the {@link ApplicationId} that is
     * associated with the code.
     */
    String DEFAULT_MAIN_BRANCH = "master";

    /**
     * Opens a {@link Git} instance for the supplied {@link ApplicationId} which can be used to manipulate the various
     * files within the repository.  When call returns the supplied {@link Git} instance will be closed.
     *
     * {@see {@link ApplicationId#forUniqueName(String)}} to understand how this method interprets the first parameter.
     *
     * @param applicationUniqueName the {@link ApplicationId} for which to open a {@link Git} instance
     * @param gitConsumer consumes an instance of {@link Git} which will be used to perform the desired actions
     */
    default void performInGit(final String applicationUniqueName, final BiConsumer<Git, Path> gitConsumer) {
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
    void performInGit(ApplicationId applicationId, BiConsumer<Git, Path> gitConsumer);

}
