package com.namazustudios.socialengine.cdnserve;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.codeserve.api.deploy.CreateDeploymentRequest;
import com.namazustudios.socialengine.dao.DeploymentDao;
import com.namazustudios.socialengine.dao.rt.GitLoader;
import com.namazustudios.socialengine.model.Deployment;
import com.namazustudios.socialengine.codeserve.api.deploy.DeploymentService;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.service.ApplicationService;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JettyDeploymentService implements DeploymentService {

    private DeploymentManager deploymentManager;

    private ApplicationService applicationService;

    private DeploymentDao deploymentDao;

    private String contentDirectory;

    private GitLoader gitLoader;

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
    public Deployment createDeployment(String applicationId, CreateDeploymentRequest deploymentRequest) {
        final Deployment deployment = new Deployment();
        deployment.setVersion(deploymentRequest.getVersion());
        deployment.setRevision(deploymentRequest.getRevision());
        final Deployment newDeployment = getDeploymentDao().createDeployment(applicationId, deployment);

        try {
            copyRepositoryContentsForRevision(applicationId, newDeployment.getRevision());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newDeployment;
    }

    private void copyRepositoryContentsForRevision(String applicationId, String revision) throws IOException {
        File gitFile = getGitLoader().getCodeDirectory(getApplicationService().getApplication(applicationId));
        Repository repo = new FileRepositoryBuilder().findGitDir(gitFile).build();
        Path tempPath = Files.createTempDirectory("static");
        OutputStream outputStream = Files.newOutputStream(tempPath);
        RevWalk walk = new RevWalk(repo);
        RevCommit commit = walk.parseCommit(ObjectId.fromString(revision));
        commit.copyRawTo(outputStream);
        Files.move(tempPath, Paths.get(getContentDirectory()));
    }

    @Override
    public void deleteDeployment(String applicationId, String deploymentId) {
        getDeploymentDao().deleteDeployment(applicationId, deploymentId);
    }

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Inject
    public void setDeploymentManager(DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    @Inject
    public void setApplicationService(ApplicationService applicationService) {
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
    private String setContentDirectory(@Named(Constants.CDN_FILE_DIRECTORY)String contentDirectory) {
        return this.contentDirectory = contentDirectory;
    }

    public GitLoader getGitLoader() {
        return gitLoader;
    }

    @Inject
    public void setGitLoader(GitLoader gitLoader) {
        this.gitLoader = gitLoader;
    }
}
