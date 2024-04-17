package dev.getelements.elements.validation;


import com.google.inject.Inject;
import dev.getelements.elements.exception.ValidationFailureException;
import dev.getelements.elements.model.leaderboard.Leaderboard;
import dev.getelements.elements.util.ValidationHelper;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

@Guice(modules = TestValidationModule.class)
public class ValidationGroupTest {

    private ValidationHelper validationHelper;

    @Test(expectedExceptions = ValidationFailureException.class)
    public void testDefaultGroup() {
        final Leaderboard leaderboard = new Leaderboard();
        leaderboard.setId("testid");
        leaderboard.setName(null);
        getValidationHelper().validateModel(leaderboard);
    }

    @Test
    public void testNestedGroupsInsertOrCreate() {
        final var testBean = new TestBean();
        final var testNestedBean = new TestNestedBean();
        testNestedBean.setTest("test");
        testBean.setTestNestedBean(testNestedBean);
            
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
