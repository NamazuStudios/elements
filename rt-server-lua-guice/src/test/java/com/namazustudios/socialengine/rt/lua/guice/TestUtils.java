package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.guice.ClasspathAssetLoaderModule;
import com.namazustudios.socialengine.test.EmbeddedTestService;
import com.namazustudios.socialengine.test.JeroMQEmbeddedTestService;

import java.util.function.Function;

public class TestUtils {

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
