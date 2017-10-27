package com.namazustudios.socialengine.rt.testkit;

import com.google.common.base.Stopwatch;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.ResourceId;
import com.namazustudios.socialengine.rt.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class SimpleTestRunner implements TestRunner {

    public static final String TESTS = "com.namazustudios.socialengine.rt.testkit.SimpleTestRunner.tests";

    private static final Logger testLogger = LoggerFactory.getLogger(LOGGER);

    private Context context;

    private Set<Test> testList;

    private ResourceService resourceService;

    @Override
    public void perform() {

        final Stopwatch suiteStopwatch = Stopwatch.createStarted();
        testLogger.info("Running all tests.");

        try {
            performSuite();
        } finally {
            final long elapsed = suiteStopwatch.elapsed(MILLISECONDS);
            testLogger.info("Completed all tests in {} msec", elapsed);
        }

    }

    private void performSuite() {
        getTestList().forEach(test -> {

            final Stopwatch testStopwatch = Stopwatch.createStarted();

            try {
                testLogger.info("Running test {}", test.getName());
                performTest(test);
            } catch (Throwable th) {
                testLogger.info("Test failed {} ");
            } finally {

                final long elapsed = testStopwatch.elapsed(MILLISECONDS);
                testLogger.info("Finished running test {} msec", elapsed);

                getResourceService().list(new Path("*")).forEachRemaining(listing -> {
                    final Path resourcePath = listing.getPath();
                    final ResourceId resourceId = listing.getResourceId();
                    testLogger.warn("{} -> {} remains after test {}.", resourcePath, resourceId, test.getName());
                });

                resourceService.removeAndCloseAllResources();

            }

        });
    }

    private void performTest(final Test test) {
        final Path path = new Path(randomUUID().toString());
        final ResourceId resourceId = getContext().getResourceContext().create(test.getModule(), path);
        final Object result = getContext().getResourceContext().invoke(resourceId, test.getTestMethod());
        testLogger.info("Successfuly got test result {}", result);
        getContext().getResourceContext().destroy(resourceId);
    }

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(Context context) {
        this.context = context;
    }

    public Set<Test> getTestList() {
        return testList;
    }

    @Inject
    public void setTestList(@Named(TESTS) Set<Test> testList) {
        this.testList = testList;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

}
