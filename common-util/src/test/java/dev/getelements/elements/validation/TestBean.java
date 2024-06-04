package dev.getelements.elements.validation;

import dev.getelements.elements.model.ValidWithGroups;
import dev.getelements.elements.model.ValidationGroups.Create;
import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.ValidationGroups.Read;
import dev.getelements.elements.model.ValidationGroups.Update;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

public class TestBean {

    @Null(groups = {Create.class, Insert.class})
    @NotNull(groups = {Read.class, Update.class})
    private String test;

    @ValidWithGroups(Read.class)
    private TestNestedBean testNestedBean;

    @Valid
    private List<@ValidWithGroups TestNestedBean> nestedBeanList;

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

    public List<TestNestedBean> getNestedBeanList() {
        return nestedBeanList;
    }

    public void setNestedBeanList(List<TestNestedBean> nestedBeanList) {
        this.nestedBeanList = nestedBeanList;
    }

}
