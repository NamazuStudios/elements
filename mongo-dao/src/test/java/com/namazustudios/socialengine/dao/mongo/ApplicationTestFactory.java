package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.model.application.Application;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;

public class ApplicationTestFactory {

    private static final AtomicInteger sequence = new AtomicInteger();

    private ApplicationDao applicationDao;

    public Application createMockApplication(final Class<?> testClass) {
        final var description = format("Test application for %s", testClass.getName());
        return createMockApplication(description);
    }

    public Application createMockApplication(final String description) {
        final Application application = new Application();
        application.setName(format("test_application_%d", sequence.getAndIncrement()));
        application.setDescription(description);
        return getApplicationDao().createOrUpdateInactiveApplication(application);
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

}
