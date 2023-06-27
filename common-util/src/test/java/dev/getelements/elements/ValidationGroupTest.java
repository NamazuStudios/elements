package dev.getelements.elements;


import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import dev.getelements.elements.exception.ValidationFailureException;
import dev.getelements.elements.model.leaderboard.Leaderboard;
import dev.getelements.elements.util.ValidationHelper;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;
import ru.vyarus.guice.validator.ValidationModule;

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
