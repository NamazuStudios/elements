package dev.getelements.elements.servlet;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.MutableAttributes;
import dev.getelements.elements.sdk.util.ImmutableAttributes;
import jakarta.servlet.ServletRequest;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class ServletRequestAttributes implements MutableAttributes {

    private final ServletRequest servletRequest;

    public ServletRequestAttributes(final ServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    @Override
    public Set<String> getAttributeNames() {

        final Enumeration<String> attributeNames = servletRequest.getAttributeNames();

        if (attributeNames != null && attributeNames.hasMoreElements()) {
            final Set<String> attributeNameList = new HashSet<>();
            while(attributeNames.hasMoreElements()) attributeNameList.add(attributeNames.nextElement());
            return attributeNameList;
        } else {
            return emptySet();
        }

    }

    @Override
    public Optional<Object> getAttributeOptional(final String name) {
        final Object value = servletRequest.getAttribute(name);
        return value == null ? empty() : of(value);
    }

    @Override
    public Attributes immutableCopy() {
        return ImmutableAttributes.copyOf(this);
    }

    @Override
    public void setAttribute(final String name, final Object obj) {
        servletRequest.setAttribute(name, obj);
    }

}
