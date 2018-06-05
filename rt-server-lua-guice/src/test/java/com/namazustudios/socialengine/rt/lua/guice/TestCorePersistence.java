package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolver;
import com.namazustudios.socialengine.rt.guice.SimpleContextModule;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.HandlerContext.HANDLER_TIMEOUT_MSEC;
import static com.namazustudios.socialengine.rt.Constants.SCHEDULER_THREADS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Guice(modules = TestCorePersistence.Module.class)
public class TestCorePersistence {

    private static final Logger logger = LoggerFactory.getLogger(TestCorePersistence.class);

    private ResourceLoader resourceLoader;

    @DataProvider
    public static Object[][] allLuaResources() {

        // This ensures that we can persist all Lua source code provided in this package, including test code.

        final Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forJavaClassPath())
            .setScanners(new ResourcesScanner()));
        final Set<String> luaResources = new TreeSet<>(reflections.getResources(Pattern.compile(".*\\.lua")));

        return luaResources
            .stream()
            .map(s -> s.replace('/', '.').substring(0, s.length() - ".lua".length()))
            .filter(s -> !"main".equals(s))              // Manifests aren't persistence aware
            .map(s -> new Object[]{s})
            .toArray(Object[][]::new);

    }

    @Test(dataProvider = "allLuaResources", invocationCount = 10)
    public void testPersistUnpersist(final String moduleName) throws IOException {

        logger.info("Testing Persistence for {}", moduleName);

        final byte[] bytes;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final Resource resource = getResourceLoader().load(moduleName)) {
            resource.setVerbose(true);
            resource.serialize(bos);
            bytes = bos.toByteArray();
        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final Resource resource = getResourceLoader().load(bis)) {
            logger.info("Successfully loaded {}", resource);
        }

    }

    @Test
    public void testIocIsRestoredAfterUnpersist() throws IOException {

        final byte[] bytes;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final Resource resource = getResourceLoader().load("test.ioc_resolve")) {

            final AtomicReference<Object> result = new AtomicReference<>();
            final AtomicReference<Throwable> exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

            resource.setVerbose(true);
            resource.serialize(bos);
            bytes = bos.toByteArray();

        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final Resource resource = getResourceLoader().load(bis, true)) {

            final AtomicReference<Object> result = new AtomicReference<>();
            final AtomicReference<Throwable> exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

        }

    }

    @Test
    public void testIocProviderIsRestoredAfterUnpersist() throws IOException {

        final byte[] bytes;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final Resource resource = getResourceLoader().load("test.ioc_resolve")) {

            final AtomicReference<Object> result = new AtomicReference<>();
            final AtomicReference<Throwable> exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve_provider")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

            resource.setVerbose(true);
            resource.serialize(bos);
            bytes = bos.toByteArray();

        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final Resource resource = getResourceLoader().load(bis, true)) {

            final AtomicReference<Object> result = new AtomicReference<>();
            final AtomicReference<Throwable> exception = new AtomicReference<>();

            resource.getMethodDispatcher("test_resolve_provider")
                    .params()
                    .dispatch(o -> result.set(o), ex -> exception.set(ex));

            assertNull(exception.get());
            assertEquals(result.get(), "Hello World!");

        }

    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            install(new LuaModule() {
                @Override
                protected void configureFeatures() {
                    enableAllFeatures();
                    bindBuiltin(TestJavaModule.class).toModuleNamed("test.java.module");
                }
            });

            install(new SimpleContextModule());

            bind(IocResolver.class).to(GuiceIoCResolver.class).asEagerSingleton();
            bind(AssetLoader.class).to(ClasspathAssetLoader.class).asEagerSingleton();
            bind(Integer.class).annotatedWith(named(SCHEDULER_THREADS)).toInstance(1);
            bind(Long.class).annotatedWith(named(HANDLER_TIMEOUT_MSEC)).toInstance(90l);

        }

    }

}
