package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.Attributes;

import javax.servlet.ServletRequest;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Collections.emptySet;

public class ServletRequestAttributes implements Attributes {

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
    public Object getAttribute(final String name) {
        return servletRequestSupplier.get().getAttribute(name);
    }

}
