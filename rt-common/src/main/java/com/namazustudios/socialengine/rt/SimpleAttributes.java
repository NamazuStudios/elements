package com.namazustudios.socialengine.rt;

import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;

public class SimpleAttributes implements Attributes {

    private Map<String, Object> attributes;

    @Override
    public Set<String> getAttributeNames() {
        return getAttributes() == null ? emptySet() : unmodifiableSet(getAttributes().keySet());
    }

    @Override
    public Object getAttribute(String name) {
        return getAttributes().get(name);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

}
