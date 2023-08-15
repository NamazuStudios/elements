package dev.getelements.elements.service.application;

import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.application.CreateApplicationRequest;
import dev.getelements.elements.model.application.UpdateApplicationRequest;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.ApplicationService;
import org.dozer.Mapper;

import javax.inject.Inject;

/**
 * {@link ApplicationService} implemented for when the current user has {@link User.Level#SUPERUSER} access.
 *
 * Created by patricktwohig on 7/10/15.
 */
public class SuperUserApplicationService implements ApplicationService {

    private ApplicationDao applicationDao;

    private ApplicationUrls applicationUrls;

    private Mapper dozerMapper;

    @Override
    public Pagination<Application> getApplications() {
        return getApplicationDao().getActiveApplications();
    }

    @Override
    public Application createApplication(CreateApplicationRequest createApplicationRequest) {
        Application application = getDozerMapper().map(createApplicationRequest, Application.class);
        return getApplicationUrls().addAllUrls(getApplicationDao().createOrUpdateInactiveApplication(application));
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count) {
        return getApplicationDao().getActiveApplications(offset, count).transform(getApplicationUrls()::addAllUrls);
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count, String search) {
        return getApplicationDao().getActiveApplications(offset, count, search).transform(getApplicationUrls()::addAllUrls);
    }

    @Override
    public Application getApplication(String nameOrId) {
        return getApplicationUrls().addAllUrls(getApplicationDao().getActiveApplication(nameOrId));
    }

    @Override
    public Application updateApplication(String nameOrId, UpdateApplicationRequest updateApplicationRequest) {
        Application application = getDozerMapper().map(updateApplicationRequest, Application.class);
        return getApplicationUrls().addAllUrls(getApplicationDao().updateActiveApplication(nameOrId, application));
    }

    @Override
    public void deleteApplication(String nameOrId) {
        getApplicationDao().softDeleteApplication(nameOrId);
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

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
    }
}
