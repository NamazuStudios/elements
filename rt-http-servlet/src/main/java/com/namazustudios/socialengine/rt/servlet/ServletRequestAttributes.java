package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.Attributes;

import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;

public class ServletRequestAttributes implements Attributes {

    private final Supplier<ServletRequest> servletRequestSupplier;

    public ServletRequestAttributes(Supplier<ServletRequest> servletRequestSupplier) {
        this.servletRequestSupplier = servletRequestSupplier;
    }

    @Override
    public List<String> getAttributeNames() {

        final Enumeration<String> attributeNames = servletRequestSupplier.get().getAttributeNames();

        if (attributeNames != null && attributeNames.hasMoreElements()) {
            final List<String> attributeNameList = new ArrayList<>();
            while(attributeNames.hasMoreElements()) attributeNameList.add(attributeNames.nextElement());
            return attributeNameList;
        } else {
            return emptyList();
        }

    }

    @Override
    public Object getAttribute(final String name) {
        return servletRequestSupplier.get().getAttribute(name);
    }

}
