package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocRoot;
import com.namazustudios.socialengine.doclet.DocRootWriter;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

import java.util.ArrayList;
import java.util.List;

public class LDocRootStubModule implements DocRoot {

    private final LDocStubModuleHeader header;

    private final List<LDocStubMethod> methods = new ArrayList<>();

    private final ModuleDefinition moduleDefinition;

    public LDocRootStubModule(final ModuleDefinition moduleDefinition) {
        header = new LDocStubModuleHeader(moduleDefinition);
        this.moduleDefinition = moduleDefinition;
    }

    public LDocStubModuleHeader getHeader() {
        return header;
    }

    public List<LDocStubMethod> getMethods() {
        return methods;
    }

    public LDocStubMethod addMethod(final String name) {
        final var method = new LDocStubMethod(name, moduleDefinition);
        getMethods().add(method);
        return method;
    }

    @Override
    public void write(final DocRootWriter writer) {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LDocStubModule{");
        sb.append("header=").append(header);
        sb.append(", methods=").append(methods);
        sb.append(", exposedModuleDefinition=").append(moduleDefinition);
        sb.append('}');
        return sb.toString();
    }

}
