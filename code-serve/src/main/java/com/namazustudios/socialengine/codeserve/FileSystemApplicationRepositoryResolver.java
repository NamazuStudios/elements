package com.namazustudios.socialengine.codeserve;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.model.application.Application;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.function.Consumer;

import static java.lang.String.format;

/**
 * This loads an instance of {@link Repository} from a place on the filesystem.
 *
 * Created by patricktwohig on 8/2/17.
 */
public class FileSystemApplicationRepositoryResolver implements ApplicationRepositoryResolver {

    private static final String GIT_DIR_SUFFIX = "git";

    private File gitStorageDirectory;

    @Override
    public Repository resolve(final Application application, Consumer<Repository> onCreate) throws Exception {

        final File repositoryDirectory = getStorageDirectoryForApplication(application);
        final FileRepository fileRepository = new FileRepository(repositoryDirectory);

        if (!fileRepository.getConfig().getFile().exists()) {
            fileRepository.create(true);
            onCreate.accept(fileRepository);
        }

        fileRepository.incrementOpen();
        return fileRepository;

    }

    final File getStorageDirectoryForApplication(final Application application) throws ServiceMayNotContinueException {

        final File absoluteStorageRoot = getGitStorageDirectory().getAbsoluteFile();

        final String dirName = format("%s.%s", application.getId(), GIT_DIR_SUFFIX);
        final File repositoryDirectory = new File(absoluteStorageRoot, dirName);

        if (!repositoryDirectory.isDirectory() && !repositoryDirectory.mkdirs()) {
            throw new ServiceMayNotContinueException("cannot create " + repositoryDirectory.getAbsolutePath());
        } else if (!repositoryDirectory.isDirectory()) {
            throw new ServiceMayNotContinueException(repositoryDirectory.getAbsolutePath() + " is not a directory.");
        }

        return repositoryDirectory;

    }

    public File getGitStorageDirectory() {
        return gitStorageDirectory;
    }

    @Inject
    public void setGitStorageDirectory(@Named(Constants.GIT_STORAGE_DIRECTORY) File gitStorageDirectory) {
        this.gitStorageDirectory = gitStorageDirectory;
    }

}
