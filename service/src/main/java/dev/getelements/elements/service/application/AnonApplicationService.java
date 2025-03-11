package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.CreateApplicationRequest;
import dev.getelements.elements.sdk.model.application.UpdateApplicationRequest;

import dev.getelements.elements.sdk.service.application.ApplicationService;
import jakarta.inject.Inject;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class AnonApplicationService implements ApplicationService {

    private ApplicationDao applicationDao;

    private ApplicationUrls applicationUrls;

    @Override
    public Application createApplication(CreateApplicationRequest applicationRequest) {
        throw new ForbiddenException();
    }

    @Override
    public Pagination<Application> getApplications() {
        throw new ForbiddenException();
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count) {
        throw new ForbiddenException();
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count, String search) {
        throw new ForbiddenException();
    }

    @Override
    public Application getApplication(String nameOrId) {
        Application application = getApplicationDao().getActiveApplicationWithoutAttributes(nameOrId);
        return getApplicationUrls().addPublicUrls(application);
    }

    @Override
    public Application updateApplication(String nameOrId, UpdateApplicationRequest applicationRequest) {
        throw new ForbiddenException();
    }

    @Override
    public void deleteApplication(String nameOrId) {
        throw new ForbiddenException();
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public ApplicationUrls getApplicationUrls() {
        return applicationUrls;
    }

    @Inject
    public void setApplicationUrls(ApplicationUrls applicationUrls) {
        this.applicationUrls = applicationUrls;
    }

}
