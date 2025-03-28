package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.CreateApplicationRequest;
import dev.getelements.elements.sdk.model.application.UpdateApplicationRequest;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import dev.getelements.elements.sdk.service.application.ApplicationService;
import jakarta.inject.Inject;

/**
 * {@link ApplicationService} implemented for when the current user has {@link User.Level#SUPERUSER} access.
 *
 * Created by patricktwohig on 7/10/15.
 */
public class SuperUserApplicationService implements ApplicationService {

    private ApplicationDao applicationDao;

    private ApplicationUrls applicationUrls;

    private MapperRegistry dozerMapperRegistry;

    @Override
    public Pagination<Application> getApplications() {
        return getApplicationDao().getActiveApplications();
    }

    @Override
    public Application createApplication(final CreateApplicationRequest createApplicationRequest) {
        final Application application = getDozerMapper().map(createApplicationRequest, Application.class);
        return getApplicationUrls().addAllUrls(getApplicationDao().createOrUpdateInactiveApplication(application));
    }

    @Override
    public Pagination<Application> getApplications(final int offset, final int count) {
        return getApplicationDao().getActiveApplications(offset, count).transform(getApplicationUrls()::addAllUrls);
    }

    @Override
    public Pagination<Application> getApplications(final int offset, final int count, final String search) {
        return getApplicationDao().getActiveApplications(offset, count, search).transform(getApplicationUrls()::addAllUrls);
    }

    @Override
    public Application getApplication(final String nameOrId) {
        return getApplicationUrls().addAllUrls(getApplicationDao().getActiveApplication(nameOrId));
    }

    @Override
    public Application updateApplication(final String nameOrId, final UpdateApplicationRequest updateApplicationRequest) {
        final Application application = getDozerMapper().map(updateApplicationRequest, Application.class);
        return getApplicationUrls().addAllUrls(getApplicationDao().updateActiveApplication(nameOrId, application));
    }

    @Override
    public void deleteApplication(final String nameOrId) {
        getApplicationDao().softDeleteApplication(nameOrId);
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(final ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public ApplicationUrls getApplicationUrls() {
        return applicationUrls;
    }

    @Inject
    public void setApplicationUrls(final ApplicationUrls applicationUrls) {
        this.applicationUrls = applicationUrls;
    }

    public MapperRegistry getDozerMapper() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapper(final MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
    }
}
