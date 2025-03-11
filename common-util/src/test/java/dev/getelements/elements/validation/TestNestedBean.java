package dev.getelements.elements.validation;

import dev.getelements.elements.sdk.model.ValidWithGroups;
import dev.getelements.elements.sdk.model.ValidationGroups;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

public class TestNestedBean {

    @Null(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @NotNull(groups = {ValidationGroups.Read.class, ValidationGroups.Update.class})
    private String test;

    @ValidWithGroups(ValidationGroups.Read.class)
    private TestNestedBean testDoubleNestedBean;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public TestNestedBean getTestDoubleNestedBean() {
        return testDoubleNestedBean;
    }

    public void setTestDoubleNestedBean(TestNestedBean testDoubleNestedBean) {
        this.testDoubleNestedBean = testDoubleNestedBean;
    }

}
