package dev.getelements.elements.validation;


import com.google.inject.Inject;
import dev.getelements.elements.sdk.model.exception.ValidationFailureException;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.leaderboard.Leaderboard;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.List;

@Guice(modules = TestValidationModule.class)
public class ValidationGroupTest {

    private ValidationHelper validationHelper;

    @DataProvider
    public static Object[][] allGroups() {
        return new Object[][] {
                new Object[] { ValidationGroups.Read.class, "test" },
                new Object[] { ValidationGroups.Update.class, "test" },
                new Object[] { ValidationGroups.Create.class, null },
                new Object[] { ValidationGroups.Insert.class, null }
        };
    }

    @Test(expectedExceptions = ValidationFailureException.class)
    public void testDefaultGroup() {
        final Leaderboard leaderboard = new Leaderboard();
        leaderboard.setId("testid");
        leaderboard.setName(null);
        getValidationHelper().validateModel(leaderboard);
    }

    @Test
    public void testNestedGroupsInsertOrCreateHappy() {

        final var testNestedBean = new TestNestedBean();
        testNestedBean.setTest("test");

        final var testBean = new TestBean();
        testBean.setTest(null);
        testBean.setTestNestedBean(testNestedBean);

        getValidationHelper().validateModel(testBean, ValidationGroups.Insert.class);
        getValidationHelper().validateModel(testBean, ValidationGroups.Create.class);

    }

    @Test(dataProvider = "allGroups" , expectedExceptions = ValidationFailureException.class)
    public void testNestedGroupsInsertOrCreateFails(final Class<?> group, final String testValue) {

        final var testNestedBean = new TestNestedBean();
        testNestedBean.setTest(null);

        final var testDoubleNestedBean = new TestNestedBean();
        testDoubleNestedBean.setTest(null);
        testDoubleNestedBean.setTestDoubleNestedBean(null);
        testNestedBean.setTestDoubleNestedBean(testDoubleNestedBean);

        final var testA = new TestNestedBean();
        final var testB = new TestNestedBean();

        final var testBean = new TestBean();
        testBean.setTest(testValue);
        testBean.setTestNestedBean(testNestedBean);
        testBean.setNestedBeanList(List.of(testA, testB));

        getValidationHelper().validateModel(testBean, group);

    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
