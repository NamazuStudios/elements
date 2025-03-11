package dev.getelements.elements.service.cdn;

import dev.getelements.elements.sdk.dao.DeploymentDao;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.application.ApplicationNotFoundException;
import dev.getelements.elements.sdk.model.exception.cdnserve.GitCloneIOException;
import dev.getelements.elements.sdk.model.exception.cdnserve.SymbolicLinkIOException;
import dev.getelements.elements.sdk.model.Deployment;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.cdn.CreateDeploymentRequest;
import dev.getelements.elements.sdk.model.cdn.UpdateDeploymentRequest;
import dev.getelements.elements.rt.git.GitApplicationAssetLoader;
import dev.getelements.elements.sdk.service.application.ApplicationService;
import dev.getelements.elements.sdk.service.cdn.CdnDeploymentService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static dev.getelements.elements.sdk.service.Constants.*;
import static java.lang.String.format;

public class SuperuserDeploymentService implements CdnDeploymentService {

    private static final Logger logger = LoggerFactory.getLogger(AnonCdnDeploymentService.class);

    private ApplicationService applicationService;

    private DeploymentDao deploymentDao;

    private String contentDirectory;

    private String cloneEndpoint;

    private String serveEndpoint;

    private GitApplicationAssetLoader gitLoader;

    @Override
    public Pagination<Deployment> getDeployments(String applicationId, int offset, int count) {
        return getDeploymentDao().getDeployments(applicationId, offset, count);
    }

    @Override
    public Deployment getDeployment(String applicationId, String deploymentId) {
        return getDeploymentDao().getDeployment(applicationId, deploymentId);
    }

    @Override
    public Deployment getCurrentDeployment(String applicationId) {
        return getDeploymentDao().getCurrentDeployment(applicationId);
    }

    @Override
    public Deployment updateDeployment(String applicationId, String version, UpdateDeploymentRequest deploymentRequest) {
        final Application app = getApplicationService().getApplication(applicationId);
        if(app == null){
            throw new ApplicationNotFoundException("Application not found with Id: " + applicationId);
        }
        final Deployment deployment = new Deployment();
        deployment.setVersion(version);
        deployment.setRevision(deploymentRequest.getRevision());
        deployment.setApplication(app);

        final Deployment updatedDeployment = getDeploymentDao().updateDeployment(applicationId, deployment);

        try {
            deleteSymbolicLink(updatedDeployment);
        } catch (IOException e) {
            try {
                getDeploymentDao().deleteDeployment(applicationId, updatedDeployment.getId());
                throw new SymbolicLinkIOException("Failed to delete previous symbolic link for version " + updatedDeployment.getVersion(), e);
            } catch (SymbolicLinkIOException symbolicLinkIOException) {
                logger.info(symbolicLinkIOException.getMessage());
                return null;
            }
        }

        copyRepositoryContentsForRevision(updatedDeployment);

        return updatedDeployment;
    }

    @Override
    public Deployment createDeployment(String applicationId, CreateDeploymentRequest deploymentRequest) {
        final Application app = getApplicationService().getApplication(applicationId);
        if(app == null){
            throw new ApplicationNotFoundException("Application not found with Id: " + applicationId);
        }
        final Deployment deployment = new Deployment();
        deployment.setVersion(deploymentRequest.getVersion());
        deployment.setRevision(deploymentRequest.getRevision());
        deployment.setApplication(app);

        final Deployment newDeployment = getDeploymentDao().createDeployment(deployment);

        copyRepositoryContentsForRevision(newDeployment);

        return newDeployment;
    }



    private void copyRepositoryContentsForRevision(Deployment newDeployment) {

        final var applicationId = newDeployment.getApplication().getId();

        getGitLoader().performInGit(applicationId, (git, workTree) -> {
            try {
                git.pull().call();
            } catch (GitAPIException e) {
                throw new InternalException(e);
            }
            try {
                Repository repo = git.getRepository();
                Path directory = Files.createDirectories(Paths.get(format("%s/%s/%s/%s", getContentDirectory(), newDeployment.getApplication().getName(), getCloneEndpoint(), UUID.randomUUID())));

                try (TreeWalk treeWalk = new TreeWalk(repo); RevWalk walk = new RevWalk(repo)) {
                    RevCommit commit = walk.parseCommit(ObjectId.fromString(newDeployment.getRevision()));
                    RevTree tree = commit.getTree();
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(false);
                    // Walk the file tree and copy all files
                    while (treeWalk.next()) {
                        copyAndCheckForSubTree(repo, treeWalk, directory);
                    }
                    walk.dispose();
                }
                createSymbolicLink(newDeployment, directory);
            }catch (IOException ex){
                try {
                    getDeploymentDao().deleteDeployment(newDeployment.getApplication().getId(), newDeployment.getId());
                    throw new GitCloneIOException("Failed to clone files for revision " + newDeployment.getRevision(), ex);
                } catch (GitCloneIOException gitCloneIOException) {
                    logger.info(gitCloneIOException.getMessage());
                }
            }
        });
    }

    private void copyAndCheckForSubTree(Repository repo, TreeWalk treeWalk, Path directory) throws IOException {
        if (treeWalk.isSubtree()) {
            String treeWalkPath = treeWalk.getPathString();
            Files.createDirectories(Paths.get(directory.toString() + "/" + treeWalkPath));
            treeWalk.enterSubtree();
            while(treeWalk.next()) {
                copyAndCheckForSubTree(repo, treeWalk, directory);
            }
        } else {
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repo.open(objectId);
            try (InputStream is = loader.openStream()){
                Files.copy(is, Paths.get(String.format("%s/%s", directory, treeWalk.getPathString())));
            }
        }
    }

    private void createSymbolicLink(Deployment newDeployment, Path pathToLink) throws IOException {
        Path directory = Files.createDirectories(Paths.get(format("%s/%s/%s", getContentDirectory(), newDeployment.getApplication().getName(), getServeEndpoint())));
        Path link = Paths.get(format("%s/%s", directory, newDeployment.getVersion()));
        if (Files.exists(link) && Files.isSymbolicLink(link)) {
            Path symLinkedPath = Files.readSymbolicLink(link);
            Files.delete(link);
            Files.delete(symLinkedPath);
        }
        String absolutePathToLink = FileSystems.getDefault().getPath(pathToLink.toString()).normalize().toAbsolutePath().toString();
        Files.createSymbolicLink(link, Paths.get(absolutePathToLink));
    }

    @Override
    public void deleteDeployment(String applicationId, String deploymentId) {
        final Deployment deploymentToRemove = getDeploymentDao().getDeployment(applicationId, deploymentId);
        try {
            deleteSymbolicLink(deploymentToRemove);
        } catch (IOException e) {
            try {
                throw new SymbolicLinkIOException("Failed to delete symbolic link for version " + deploymentToRemove.getVersion(), e);
            } catch (SymbolicLinkIOException symbolicLinkIOException) {
                logger.info(symbolicLinkIOException.getMessage());
            }
        }
        getDeploymentDao().deleteDeployment(applicationId, deploymentId);
    }

    private void deleteSymbolicLink(Deployment deployment) throws IOException {
        Path link = Paths.get(format("%s/%s/%s/%s", getContentDirectory(), deployment.getApplication().getName(), getServeEndpoint(), deployment.getVersion()));
        if (Files.exists(link) && Files.isSymbolicLink(link)) {
            Path symLinkedPath = Files.readSymbolicLink(link);
            removeDirectory(symLinkedPath.toFile());
            Files.delete(link);
        }
    }

    private void removeDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File aFile : files) {
                    removeDirectory(aFile);
                }
            }
            dir.delete();
        } else {
            dir.delete();
        }
    }

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    @Inject
    public void setApplicationService(@Named(UNSCOPED) ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public DeploymentDao getDeploymentDao() {
        return deploymentDao;
    }

    @Inject
    public void setDeploymentDao(DeploymentDao deploymentDao) {
        this.deploymentDao = deploymentDao;
    }

    private String getContentDirectory() {
        return contentDirectory;
    }

    @Inject
    private String setContentDirectory(@Named(CDN_FILE_DIRECTORY)String contentDirectory) {
        return this.contentDirectory = contentDirectory;
    }

    private String getCloneEndpoint() {
        return cloneEndpoint;
    }

    @Inject
    private void setCloneEndpoint(@Named(CDN_CLONE_ENDPOINT)String cloneEndpoint) {
        this.cloneEndpoint = cloneEndpoint;
    }

    private String getServeEndpoint() {
        return serveEndpoint;
    }

    @Inject
    private void setServeEndpoint(@Named(CDN_SERVE_ENDPOINT)String serveEndpoint) {
        this.serveEndpoint = serveEndpoint;
    }

    public GitApplicationAssetLoader getGitLoader() {
        return gitLoader;
    }

    @Inject
    public void setGitLoader(@Named(GIT_REPO) GitApplicationAssetLoader gitLoader) {
        this.gitLoader = gitLoader;
    }

}
