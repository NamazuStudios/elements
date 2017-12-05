package com.namazustudios.socialengine.rt.testkit;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.FileAssetLoader;
import com.namazustudios.socialengine.rt.guice.SimpleContextModule;
import com.namazustudios.socialengine.rt.guice.SimpleServicesModule;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;

import java.util.*;
import java.util.stream.Stream;

import static com.google.inject.name.Names.bindProperties;
import static com.google.inject.name.Names.named;

public class TestRunnerModule extends AbstractModule {

    private String projectRoot;

    private final Set<Test> tests = new LinkedHashSet<>();

    @Override
    protected void configure() {

        if (tests.isEmpty()) {
            addError("No tests defined.  Please define tests.");
        }

        install(new LuaModule());
        install(new SimpleContextModule());

        final Multibinder<Test> testMultibinder = Multibinder.newSetBinder(binder(), Test .class, named(SimpleTestRunner.TESTS));
        tests.forEach(testMultibinder.addBinding()::toInstance);

        bind(TestRunner.class).to(SimpleTestRunner.class);
        bind(AssetLoader.class).toProvider(() -> new FileAssetLoader(projectRoot)).asEagerSingleton();

    }

    /**
     * Returns the number of tests defined.
     *
     * @return the number of tests
     */
    public int testCount() {
        return tests.size();
    }

    /**
     * Specifies the project root for the project.
     *
     * @param projectRoot the project root
     * @return this instance
     */
    public TestRunnerModule withProjectRoot(final String projectRoot) {
        this.projectRoot = projectRoot;
        return this;
    }

    /**
     * Adds all tests from the supplied {@link Iterable<String>}.  Each test is supplied in the format of
     * "module.name:test."
     *
     * @param tests a {@link Iterable} of test specs to add
     * @return this instance
     */
    public TestRunnerModule addTests(final Stream<String> tests) {
        tests.map(this::parse).forEach(this.tests::add);
        return this;
    }

    /**
     * Adds all tests from the supplied {@link Iterable<String>}.  Each test is supplied in the format of
     * "module.name:test."
     *
     * @param tests a {@link Collection} of test specs to add
     * @return this instance
     */
    public TestRunnerModule addTests(final Collection<String> tests) {
        addTests(tests.stream());
        return this;
    }

    private Test parse(final String test) {

        final String[] strings = test.trim().split(":");

        if (strings.length != 2) {
            throw new IllegalArgumentException("Malformed test specification.  Expecting <module>:<method>.  Got: " + test);
        }

        final String module = strings[0];
        final String method = strings[1];

        return new Test() {

            @Override
            public String getName() {
                return test;
            }

            @Override
            public String getModule() {
                return module;
            }

            @Override
            public String getTestMethod() {
                return method;
            }
        };

    }

}
