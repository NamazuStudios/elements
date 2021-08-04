package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocRoot;
import com.namazustudios.socialengine.doclet.DocRootWriter;

import java.util.ArrayList;
import java.util.List;

public class LDocRootStubClass implements DocRoot {

    private final LDocStubClassHeader header;

    private final List<LDocStubMethod> methods = new ArrayList<>();

    public LDocRootStubClass(final String name) {
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

    @Override
    public void write(final DocRootWriter writer) {

    }

}
