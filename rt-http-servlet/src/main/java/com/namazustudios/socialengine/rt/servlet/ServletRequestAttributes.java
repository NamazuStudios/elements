package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.MutableAttributes;

import javax.servlet.ServletRequest;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class ServletRequestAttributes implements MutableAttributes {

    private final Supplier<ServletRequest> servletRequestSupplier;

    public ServletRequestAttributes(Supplier<ServletRequest> servletRequestSupplier) {
        this.servletRequestSupplier = servletRequestSupplier;
    }

    @Override
    public Set<String> getAttributeNames() {

        final Enumeration<String> attributeNames = servletRequestSupplier.get().getAttributeNames();

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
        final Object value = servletRequestSupplier.get().getAttribute(name);
        return value == null ? empty() : of(value);
    }

    @Override
    public void setAttribute(final String name, final Object obj) {
        servletRequestSupplier.get().setAttribute(name, obj);
    }

}
