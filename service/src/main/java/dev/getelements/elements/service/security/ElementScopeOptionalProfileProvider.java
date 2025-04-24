package dev.getelements.elements.service.security;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.model.profile.Profile;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.Optional;

import static dev.getelements.elements.sdk.model.profile.Profile.PROFILE_ATTRIBUTE;

public class ElementScopeOptionalProfileProvider implements Provider<Optional<Profile>> {

    private Element element;

    @Override
    public Optional<Profile> get() {
        return getElement()
                .getCurrentScope()
                .getMutableAttributes()
                .getAttributeOptional(PROFILE_ATTRIBUTE)
                .filter(Profile.class::isInstance)
                .map(Profile.class::cast);
    }

    public Element getElement() {
        return element;
    }

    @Inject
    public void setElement(Element element) {
        this.element = element;
    }

}
