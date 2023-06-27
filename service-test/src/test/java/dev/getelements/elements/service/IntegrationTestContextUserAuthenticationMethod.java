package dev.getelements.elements.service;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.security.UserAuthenticationMethod;

import javax.inject.Inject;

import static dev.getelements.elements.model.user.User.USER_ATTRIBUTE;

public class IntegrationTestContextUserAuthenticationMethod implements UserAuthenticationMethod {

    private TestScope.Context testScopeContext;

    @Override
    public User attempt() throws ForbiddenException {
        return getTestScopeContext()
                .getAttributes()
                .getAttributeOptional(USER_ATTRIBUTE)
                .map(User.class::cast)
                .orElseThrow(ForbiddenException::new);
    }

    public TestScope.Context getTestScopeContext() {
        return testScopeContext;
    }

    @Inject
    public void setTestScopeContext(TestScope.Context testScopeContext) {
        this.testScopeContext = testScopeContext;
    }

}
