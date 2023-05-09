package dev.getelements.elements.rt.lua.guice;

import dev.getelements.elements.rt.guice.ClasspathAssetLoaderModule;
import dev.getelements.elements.rt.remote.Worker;
import dev.getelements.elements.test.EmbeddedTestService;
import dev.getelements.elements.test.JeroMQEmbeddedTestService;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;

public class TestLiveWorkerMutation {

    private static TestLiveWorkerMutation getXodusTest(final Function<EmbeddedTestService, TestLiveWorkerMutation> ctor) {
        final var embeddedTestService = new JeroMQEmbeddedTestService()
            .withClient()
            .withNodeModuleFactory(nodeId -> List.of(
                    new LuaModule(),
                    new JavaEventModule(),
                    new ClasspathAssetLoaderModule().withDefaultPackageRoot()
            ))
            .withXodusWorker()
            .withDefaultHttpClient()
            .start();

        return ctor.apply(embeddedTestService);
    }

    private static TestLiveWorkerMutation getUnixFSTest(final Function<EmbeddedTestService, TestLiveWorkerMutation> ctor) {

        final var embeddedTestService = new JeroMQEmbeddedTestService()
                .withClient()
                .withNodeModuleFactory(nodeId -> List.of(
                        new LuaModule(),
                        new JavaEventModule(),
                        new ClasspathAssetLoaderModule().withDefaultPackageRoot()
                ))
                .withXodusWorker()
                .withDefaultHttpClient()
                .start();

        return ctor.apply(embeddedTestService);

    }

    @Factory
    public static Object[] getIntegrationTests() {
        return new Object[] {
            getXodusTest(TestLiveWorkerMutation::new),
            getUnixFSTest(TestLiveWorkerMutation::new)
        };
    }

    private final Worker worker;

    private final EmbeddedTestService embeddedTestService;

    public TestLiveWorkerMutation(final EmbeddedTestService embeddedTestService) {
        this.worker = embeddedTestService
            .getWorker()
            .getWorker();
        this.embeddedTestService = embeddedTestService;
    }

    public Stream<String> getApplicationNames() {
        return IntStream
            .range(0, 10)
            .mapToObj(i ->format("Mock Application %d", i));
    }

    @DataProvider
    public Object[][] getApplicationNamesProvider() {
        return getApplicationNames()
            .map(s -> new Object[]{s})
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getApplicationNamesProvider")
    public void testAddNodeIndividually(final String mockApplicationName) {
        try (var mutator = worker.beginMutation()) {
            mutator.addNode(mockApplicationName);
            mutator.commit();
        }
    }

    @Test(dataProvider = "getApplicationNamesProvider", dependsOnMethods = "testAddNodeIndividually")
    public void testRestartNodeIndividually(final String mockApplicationName) {
        try (var mutator = worker.beginMutation()) {
            mutator.restartNode(mockApplicationName);
            mutator.commit();
        }
    }

    @Test(dataProvider = "getApplicationNamesProvider", dependsOnMethods = "testRestartNodeIndividually")
    public void testStopNodeIndividually(final String mockApplicationName) {
        try (var mutator = worker.beginMutation()) {
            mutator.removeNode(mockApplicationName);
            mutator.commit();
        }
    }

    @Test(dependsOnMethods = "testStopNodeIndividually")
    public void testStartAll() {
        try (var mutator = worker.beginMutation()) {
            getApplicationNames().forEach(mutator::addNode);
            mutator.commit();
        }
    }

    @Test(dependsOnMethods = "testStartAll")
    public void testRestartAll() {
        try (var mutator = worker.beginMutation()) {
            getApplicationNames().forEach(mutator::restartNode);
            mutator.commit();
        }
    }

    @Test(dependsOnMethods = "testRestartAll")
    public void testStopAll() {
        try (var mutator = worker.beginMutation()) {
            getApplicationNames().forEach(mutator::removeNode);
            mutator.commit();
        }
    }

    @AfterClass
    public void shutdownService() {
        embeddedTestService.close();
    }

}
