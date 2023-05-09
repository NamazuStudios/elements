package dev.getelements.elements;

import dev.getelements.elements.rt.guice.ClasspathAssetLoaderModule;
import dev.getelements.elements.rt.lua.guice.JavaEventModule;
import dev.getelements.elements.rt.lua.guice.LuaModule;
import dev.getelements.elements.rt.transact.JournalTransactionalResourceServicePersistenceEnvironment;
import dev.getelements.elements.rt.transact.unix.UnixFSJournalTransactionalPersistenceDriver;
import dev.getelements.elements.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment;
import dev.getelements.elements.test.EmbeddedTestService;
import dev.getelements.elements.test.JeroMQEmbeddedTestService;

import java.util.function.Function;

import static com.google.inject.Guice.createInjector;

public class TestUtils {

    /**
     * Creates a test case with the {@link XodusTransactionalResourceServicePersistenceEnvironment}
     *
     * @param testClass the type to construct
     * @param <T> the type to return
     * @return the constructed test case
     */
    public static <T> T getXodusTest(Class<T> testClass) {
        final var module = new UnitTestModule(new JeroMQEmbeddedTestService().withXodusWorker());
        final var injector = createInjector(module);
        return injector.getInstance(testClass);
    }

    /**
     * Creates a test case with the {@link JournalTransactionalResourceServicePersistenceEnvironment} backed by the
     * {@link UnixFSJournalTransactionalPersistenceDriver}
     *
     * @param testClass the type to construct
     * @param <T> the type to return
     * @return the constructed test case
     */
    public static <T> T getUnixFSTest(Class<T> testClass) {
        final var module = new UnitTestModule(new JeroMQEmbeddedTestService().withUnixFSWorker());
        final var injector = createInjector(module);
        return injector.getInstance(testClass);
    }


    /**
     * Creates a test case with the {@link XodusTransactionalResourceServicePersistenceEnvironment}
     *
     * @param testClass the type to construct
     * @param <T> the type to return
     * @return the constructed test case
     */
    public static <T> T getXodusIntegrationTest(Class<T> testClass) {
        final var module = new IntegrationTestModule(new JeroMQEmbeddedTestService().withXodusWorker());
        final var injector = createInjector(module);
        return injector.getInstance(testClass);
    }

    /**
     * Creates a test case with the {@link JournalTransactionalResourceServicePersistenceEnvironment} backed by the
     * {@link UnixFSJournalTransactionalPersistenceDriver}
     *
     * @param testClass the type to construct
     * @param <T> the type to return
     * @return the constructed test case
     */
    public static <T> T getUnixFSIntegrationTest(Class<T> testClass) {
        final var module = new IntegrationTestModule(new JeroMQEmbeddedTestService().withUnixFSWorker());
        final var injector = createInjector(module);
        return injector.getInstance(testClass);
    }
}
