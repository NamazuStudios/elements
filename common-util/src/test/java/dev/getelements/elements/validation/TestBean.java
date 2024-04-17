package dev.getelements.elements.validation;

import dev.getelements.elements.model.ValidForGroups;
import dev.getelements.elements.model.ValidationGroups;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

public class TestBean {

    @Null(groups = {ValidationGroups.Create.class, ValidationGroups.Insert.class})
    @NotNull(groups = {ValidationGroups.Read.class, ValidationGroups.Update.class})
    private String test;

    @ValidForGroups
    private TestNestedBean testNestedBean;


    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public TestNestedBean getTestNestedBean() {
        return testNestedBean;
    }

    public void setTestNestedBean(TestNestedBean testNestedBean) {
        this.testNestedBean = testNestedBean;
    }

}
