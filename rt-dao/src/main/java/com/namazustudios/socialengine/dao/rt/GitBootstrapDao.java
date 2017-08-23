package com.namazustudios.socialengine.dao.rt;

import com.google.common.io.ByteStreams;
import com.namazustudios.socialengine.dao.BootstrapDao;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Bootstrapper;
import com.namazustudios.socialengine.rt.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class GitBootstrapDao implements BootstrapDao {

    private static final Logger logger = LoggerFactory.getLogger(GitBootstrapDao.class);

    private GitLoader gitLoader;

    private Function<Application, Bootstrapper> applicationBootstrapperFunction;

    @Override
    public void bootstrap(final Application application) {
        final Bootstrapper bootstrapper = getApplicationBootstrapperFunction().apply(application);
        getGitLoader().performInGit(application, (g, f) -> doBootstrap(bootstrapper, g, f));
    }

    private void doBootstrap(final Bootstrapper bootstrapper,
                             final Git git, final Function<Path, OutputStream> outputStreamSupplier) {
        checkoutMainBranch(git);
        createAndAddResources(bootstrapper, git, outputStreamSupplier);
        commitBootstrap(git);
        pushBootstrap(git);
    }

    private void checkoutMainBranch(final Git git) {
        try {
            git.checkout()
                .setCreateBranch(true)
                .setName(GitLoader.MAIN_BRANCH)
                .call();
        } catch (GitAPIException ex) {
            throw new InternalException(ex);
        }
    }

    private void createAndAddResources(final Bootstrapper bootstrapper,
                                       final Git git, final Function<Path, OutputStream> outputStreamSupplier) {
        final Map<Path, Supplier<InputStream>> pathSupplierMap = bootstrapper.getBootstrapResources();
        pathSupplierMap.forEach((k,v) -> createAndAddResource(k, v, git, outputStreamSupplier));
    }

    private void createAndAddResource(
            final Path path, final Supplier<InputStream> inputStreamSupplier,
            final Git git, final Function<Path, OutputStream> outputStreamSupplier) {
        createResource(path, inputStreamSupplier, outputStreamSupplier);
        addResource(path, git);
    }

    private void createResource(final Path path,
                                final Supplier<InputStream> inputStreamSupplier,
                                final Function<Path, OutputStream> outputStreamSupplier) {
        try (final InputStream inputStream = inputStreamSupplier.get();
             final OutputStream outputStream = outputStreamSupplier.apply(path)) {
            logger.info("Copying {} to git directory.", path.toNormalizedPathString());
            ByteStreams.copy(inputStream, outputStream);
            logger.info("Copied {} to git directory.", path.toNormalizedPathString());
        } catch (IOException ex) {
            throw new InternalException(ex);
        }
    }

    private void commitBootstrap(final Git git) {
        try {
            git.commit().setMessage("Initial Commit").call();
        } catch (GitAPIException ex) {
            throw new InternalException(ex);
        }
    }

    private void pushBootstrap(final Git git) {
        try {
            git.push().call();
        } catch (GitAPIException ex) {
            throw new InternalException(ex);
        }
    }

    private void addResource(final Path path, final Git git) {
        try {
            logger.info("Adding {}", path.toNormalizedPathString());
            git.add().addFilepattern(path.toNormalizedPathString()).call();
            logger.info("Added {}", path.toNormalizedPathString());
        } catch (GitAPIException ex) {
            throw new InternalException(ex);
        }
    }

    public GitLoader getGitLoader() {
        return gitLoader;
    }

    @Inject
    public void setGitLoader(GitLoader gitLoader) {
        this.gitLoader = gitLoader;
    }

    public Function<Application, Bootstrapper> getApplicationBootstrapperFunction() {
        return applicationBootstrapperFunction;
    }

    @Inject
    public void setApplicationBootstrapperFunction(Function<Application, Bootstrapper> applicationBootstrapperFunction) {
        this.applicationBootstrapperFunction = applicationBootstrapperFunction;
    }

}
