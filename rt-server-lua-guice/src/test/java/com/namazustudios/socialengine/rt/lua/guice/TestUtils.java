package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.MongoTestInstanceModule;
import com.namazustudios.socialengine.rt.guice.ClasspathAssetLoaderModule;
import com.namazustudios.socialengine.rt.transact.JournalTransactionalResourceServicePersistenceEnvironment;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSJournalTransactionalPersistenceDriver;
import com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment;
import com.namazustudios.socialengine.test.EmbeddedTestService;
import com.namazustudios.socialengine.test.JeroMQEmbeddedTestService;

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

    /**
     * Creates a test case with the {@link XodusTransactionalResourceServicePersistenceEnvironment}
     *
     * @param ctor the constructor reference for the test case
     * @param <T> the type to return
     * @return the constructed test case
     */
    public static <T> T getXodusTestWithMongo(final Function<EmbeddedTestService, T> ctor) {

        final var embeddedTestService = new JeroMQEmbeddedTestService()
                .withClient()
                .withApplicationNode()
                .withNodeModules(new LuaModule())
                .withNodeModules(new JavaEventModule())
                .withNodeModules(new ClasspathAssetLoaderModule().withDefaultPackageRoot())
                .withNodeModules(new MongoTestInstanceModule())
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
    public static <T> T getUnixFSTestWithMongo(final Function<EmbeddedTestService, T> ctor) {

        final var embeddedTestService = new JeroMQEmbeddedTestService()
                .withClient()
                .withApplicationNode()
                .withNodeModules(new LuaModule())
                .withNodeModules(new JavaEventModule())
                .withNodeModules(new ClasspathAssetLoaderModule().withDefaultPackageRoot())
                .withNodeModules(new MongoTestInstanceModule())
                .endApplication()
                .withUnixFSWorker()
                .withDefaultHttpClient()
                .start();

        return ctor.apply(embeddedTestService);

    }
}
