package com.namazustudios.socialengine.doclet.lua;

import java.util.ArrayList;
import java.util.List;

public class LDocStubClass implements LDocStub {

    private final LDocStubClassHeader header;

    private final List<LDocStubMethod> methods = new ArrayList<>();

    public LDocStubClass(final String name) {
        header = new LDocStubClassHeader(name);
    }

    public LDocStubClassHeader getHeader() {
        return header;
    }

    public List<LDocStubMethod> getMethods() {
        return methods;
    }

    public LDocStubMethod addMethod(final String name) {
        final var method = new LDocStubMethod(name);
        getMethods().add(method);
        return method;
    }

}
