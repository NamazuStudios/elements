package dev.getelements.elements.rest;

import com.google.inject.AbstractModule;
import dev.getelements.elements.service.BuildPropertiesVersionService;
import dev.getelements.elements.service.VersionService;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rest.TestUtils.TEST_INSTANCE;

public class TestInstanceModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(VersionService.class)
                .annotatedWith(named(TEST_INSTANCE))
                .to(BuildPropertiesVersionService.class);

    }

}
