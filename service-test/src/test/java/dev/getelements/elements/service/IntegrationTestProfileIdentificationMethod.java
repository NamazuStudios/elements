package dev.getelements.elements.service;

import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.security.ProfileIdentificationMethod;

import jakarta.inject.Inject;

import java.util.Optional;

import static dev.getelements.elements.sdk.model.profile.Profile.PROFILE_ATTRIBUTE;

public class IntegrationTestProfileIdentificationMethod implements ProfileIdentificationMethod {

    private TestScope.Context testScopeContext;

    @Override
    public Optional<Profile> attempt() {
        return getTestScopeContext()
                .getAttributes()
                .getAttributeOptional(PROFILE_ATTRIBUTE)
                .map(Profile.class::cast);
    }

    public TestScope.Context getTestScopeContext() {
        return testScopeContext;
    }

    @Inject
    public void setTestScopeContext(TestScope.Context testScopeContext) {
        this.testScopeContext = testScopeContext;
    }

}
