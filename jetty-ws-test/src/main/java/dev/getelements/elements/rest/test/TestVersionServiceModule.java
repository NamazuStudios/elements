package dev.getelements.elements.rest.test;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.service.version.VersionService;
import dev.getelements.elements.service.version.BuildPropertiesVersionService;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rest.test.TestUtils.TEST_INSTANCE;

public class TestVersionServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(VersionService.class)
                .annotatedWith(named(TEST_INSTANCE))
                .to(BuildPropertiesVersionService.class);
    }

}
