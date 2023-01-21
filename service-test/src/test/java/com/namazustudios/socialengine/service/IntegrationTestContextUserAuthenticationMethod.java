package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.UserAuthenticationMethod;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.user.User.USER_ATTRIBUTE;

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
