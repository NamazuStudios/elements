package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

import java.util.ArrayList;
import java.util.List;

public class LDocStubModule implements LDocStub {

    private final LDocStubHeader header;

    private final List<LDocStubMethod> methods = new ArrayList<>();

    private final ModuleDefinition moduleDefinition;

    public LDocStubModule(final ModuleDefinition moduleDefinition) {
        header = new LDocStubHeader(moduleDefinition);
        this.moduleDefinition = moduleDefinition;
    }

    public LDocStubHeader getHeader() {
        return header;
    }

    public List<LDocStubMethod> getMethods() {
        return methods;
    }

    public LDocStubMethod addMethod(final String name) {
        final var method = new LDocStubMethod(moduleDefinition, name);
        getMethods().add(method);
        return method;
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
