package dev.getelements.elements.service.security;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.model.session.Session;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.model.session.Session.SESSION_ATTRIBUTE;

public class ElementScopedSessionProvider implements Provider<Session> {

    private Element element;

    @Override
    public Session get() {
        return getElement()
                .getCurrentScope()
                .getMutableAttributes()
                .getAttributeOptional(SESSION_ATTRIBUTE)
                .filter(Session.class::isInstance)
                .map(Session.class::cast)
                .orElseThrow(() -> new IllegalStateException("Session is not present"));
    }

    public Element getElement() {
        return element;
    }

    @Inject
    public void setElement(Element element) {
        this.element = element;
    }

}
