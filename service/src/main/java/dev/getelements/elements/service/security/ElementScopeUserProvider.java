package dev.getelements.elements.service.security;

import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.model.user.User;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.model.user.User.USER_ATTRIBUTE;

public class ElementScopeUserProvider implements Provider<User> {

    private Element element;

    @Override
    public User get() {
        return getElement()
                .getCurrentScope()
                .getMutableAttributes()
                .getAttributeOptional(USER_ATTRIBUTE)
                .filter(User.class::isInstance)
                .map(User.class::cast)
                .orElseGet(User::getUnprivileged);
    }

    public Element getElement() {
        return element;
    }

    @Inject
    public void setElement(Element element) {
        this.element = element;
    }

}
