package com.namazustudios.socialengine.rt.testkit;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.FileAssetLoader;
import com.namazustudios.socialengine.rt.guice.SimpleContextModule;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;

import java.util.*;
import java.util.stream.Stream;

import static com.google.inject.name.Names.named;

public class TestRunnerModule extends AbstractModule {

    private String projectRoot;

    private final Set<Test> tests = new LinkedHashSet<>();

    @Override
    protected void configure() {

        if (tests.isEmpty()) {
            addError("No tests defined.  Please define tests.");
        }

        install(new SimpleContextModule());

        final Multibinder<Test> testMultibinder = Multibinder.newSetBinder(binder(), Test .class, named(SimpleTestRunner.TESTS));
        tests.forEach(t -> testMultibinder.addBinding().toInstance(t));

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

        return new ModuleTest(test, module, method);

    }

    private class ModuleTest implements Test {

        private final String test;

        private final String module;

        private final String method;

        public ModuleTest(String test, String module, String method) {
            this.test = test;
            this.module = module;
            this.method = method;
        }

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

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (!(o instanceof ModuleTest)) return false;

            ModuleTest that = (ModuleTest) o;

            if (test != null ? !test.equals(that.test) : that.test != null) return false;
            if (getModule() != null ? !getModule().equals(that.getModule()) : that.getModule() != null) return false;
            return method != null ? method.equals(that.method) : that.method == null;
        }

        @Override
        public int hashCode() {
            int result = test != null ? test.hashCode() : 0;
            result = 31 * result + (getModule() != null ? getModule().hashCode() : 0);
            result = 31 * result + (method != null ? method.hashCode() : 0);
            return result;
        }

    }
}
