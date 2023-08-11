package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.exception.InvalidParameterException;
import dev.getelements.elements.model.application.Application;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toMap;

public class ApplicationTestFactory {

    private static final AtomicInteger sequence = new AtomicInteger();

    private ApplicationDao applicationDao;

    public Application createMockApplication(final Class<?> testClass) {
        final var description = format("Test application for %s", testClass.getName());
        return createMockApplication(description);
    }

    public Application createMockApplication(final String description) {
        final Application application = createAtomicApplication(description);
        return getApplicationDao().createOrUpdateInactiveApplication(application);
    }

    public Application createMockApplication(final String description, final Map<String, Object> attributes) {
        Application application = createAtomicApplication(description);
        application.setAttributes(attributes);
        return getApplicationDao().createOrUpdateInactiveApplication(application);
    }

    public Application createAtomicApplication(final String description) {
        return createAtomicApplication(format("test_application_%d", sequence.getAndIncrement()), description);
    }

    public Application createAtomicApplication(final String name, final String description) {
        final Application application = new Application();
        application.setName(name);
        application.setDescription(description);
        return application;
    }

    public Map<String, Object> createSingleAttributesMock(String key, String value) {
        return singletonMap(key, value);
    }

    public Application createMockApplicationWithSingleAttribute(final String description, String key, String value) {
        return createMockApplication(description, createSingleAttributesMock(key, value));
    }

    public Map<String, Object> createMockAttributes(List<String> keys, List<String> values) {
        if (keys.size() != values.size()) {
            throw new InvalidParameterException("Keys size must equal values size");
        }
        return IntStream.range(0, keys.size()).boxed().collect(toMap(keys::get, values::get));
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

}
