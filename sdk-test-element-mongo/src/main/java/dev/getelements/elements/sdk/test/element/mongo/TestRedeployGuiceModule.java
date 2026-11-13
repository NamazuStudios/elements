package dev.getelements.elements.sdk.test.element.mongo;

import com.google.inject.AbstractModule;

/**
 * Binds {@link TestRedeployIndexInitializer} as an eager singleton so its constructor -- which exercises the
 * shared Morphia {@code Datastore} for this Element's entity -- runs at Element injector-build time, inside
 * the runtime service's load phase (see the javadoc on {@link TestRedeployIndexInitializer} and issue #209).
 */
public class TestRedeployGuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TestRedeployIndexInitializer.class).asEagerSingleton();
    }

}
