package com.namazustudios.socialengine;


import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.exception.ValidationFailureException;
import com.namazustudios.socialengine.model.leaderboard.Leaderboard;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.bval.guice.ValidationModule;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = ValidationGroupTest.Module.class)
public class ValidationGroupTest {

    private ValidationHelper validationHelper;

    @Test(expectedExceptions = ValidationFailureException.class)
    public void testDefaultGroup() {
        final Leaderboard leaderboard = new Leaderboard();
        leaderboard.setId("testid");
        leaderboard.setName(null);
        getValidationHelper().validateModel(leaderboard);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            install(new ValidationModule());
        }

    }

}
