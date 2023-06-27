package dev.getelements.elements.rt.lua.guice;

import dev.getelements.elements.rt.guice.ClasspathAssetLoaderModule;
import dev.getelements.elements.rt.jersey.JerseyHttpClientModule;
import dev.getelements.elements.rt.transact.JournalTransactionalResourceServicePersistenceEnvironment;
import dev.getelements.elements.rt.transact.unix.UnixFSJournalTransactionalPersistenceDriver;
import dev.getelements.elements.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment;
import dev.getelements.elements.test.EmbeddedTestService;
import dev.getelements.elements.test.JeroMQEmbeddedTestService;

import java.util.function.Function;

public class TestUtils {

    /**
     * Creates a test case with the {@link XodusTransactionalResourceServicePersistenceEnvironment}
     *
     * @param ctor the constructor reference for the test case
     * @param <T> the type to return
     * @return the constructed test case
     */
    public static <T> T getXodusTest(final Function<EmbeddedTestService, T> ctor) {

        final var embeddedTestService = new JeroMQEmbeddedTestService()
            .withClient()
            .withApplicationNode()
                .withNodeModules(new LuaModule())
                .withNodeModules(new JavaEventModule())
                .withNodeModules(new ClasspathAssetLoaderModule().withDefaultPackageRoot())
            .endApplication()
            .withXodusWorker()
            .withDefaultHttpClient()
            .start();

        return ctor.apply(embeddedTestService);

    }

    /**
     * Creates a test case with the {@link JournalTransactionalResourceServicePersistenceEnvironment} backed by the
     * {@link UnixFSJournalTransactionalPersistenceDriver}
     *
     * @param ctor the constructor reference for the test case
     * @param <T> the type to return
     * @return the constructed test case
     */
    public static <T> T getUnixFSTest(final Function<EmbeddedTestService, T> ctor) {

        final var embeddedTestService = new JeroMQEmbeddedTestService()
            .withClient()
            .withApplicationNode()
                .withNodeModules(new LuaModule())
                .withNodeModules(new JavaEventModule())
                .withNodeModules(new ClasspathAssetLoaderModule().withDefaultPackageRoot())
            .endApplication()
            .withUnixFSWorker()
            .withDefaultHttpClient()
            .start();

        return ctor.apply(embeddedTestService);

    }

}
