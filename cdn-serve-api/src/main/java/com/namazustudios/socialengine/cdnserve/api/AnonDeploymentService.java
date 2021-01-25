package com.namazustudios.socialengine.cdnserve.api;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.DeploymentDao;
import com.namazustudios.socialengine.dao.rt.GitLoader;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.application.ApplicationNotFoundException;
import com.namazustudios.socialengine.exception.cdnserve.GitCloneIOException;
import com.namazustudios.socialengine.exception.cdnserve.SymbolicLinkIOException;
import com.namazustudios.socialengine.model.Deployment;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.service.ApplicationService;
import org.eclipse.jetty.deploy.DeploymentManager;
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

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

import static java.lang.String.format;

public class AnonDeploymentService implements DeploymentService {

    private DeploymentDao deploymentDao;

    @Override
    public Pagination<Deployment> getDeployments(String applicationId, int offset, int count) {
        throw new ForbiddenException();
    }

    @Override
    public Deployment getDeployment(String applicationId, String deploymentId) {
        throw new ForbiddenException();
    }

    @Override
    public Deployment getCurrentDeployment(String applicationId) {
        return getDeploymentDao().getCurrentDeployment(applicationId);
    }

    @Override
    public Deployment updateDeployment(String applicationId, String version, UpdateDeploymentRequest deploymentRequest) {
        throw new ForbiddenException();
    }

    @Override
    public Deployment createDeployment(String applicationId, CreateDeploymentRequest deploymentRequest) {
        throw new ForbiddenException();
    }

    @Override
    public void deleteDeployment(String applicationId, String deploymentId) {
        throw new ForbiddenException();
    }

    public DeploymentDao getDeploymentDao() {
        return deploymentDao;
    }

    @Inject
    public void setDeploymentDao(DeploymentDao deploymentDao) {
        this.deploymentDao = deploymentDao;
    }
}
