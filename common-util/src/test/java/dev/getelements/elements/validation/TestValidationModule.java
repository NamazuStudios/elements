package dev.getelements.elements.validation;

import com.google.inject.AbstractModule;
import ru.vyarus.guice.validator.ValidationModule;

public class TestValidationModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new ValidationModule());
    }

}
