package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.profile.UnidentifiedProfileException;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.security.ProfileIdentificationMethod;

import javax.inject.Inject;

import static com.namazustudios.socialengine.model.profile.Profile.PROFILE_ATTRIBUTE;

public class IntegrationTestProfileIdentificationMethod implements ProfileIdentificationMethod {

    private TestScope.Context testScopeContext;

    @Override
    public Profile attempt() throws UnidentifiedProfileException {
        return getTestScopeContext()
                .getAttributes()
                .getAttributeOptional(PROFILE_ATTRIBUTE)
                .map(Profile.class::cast)
                .orElseThrow(UnidentifiedProfileException::new);
    }

    public TestScope.Context getTestScopeContext() {
        return testScopeContext;
    }

    @Inject
    public void setTestScopeContext(TestScope.Context testScopeContext) {
        this.testScopeContext = testScopeContext;
    }

}
