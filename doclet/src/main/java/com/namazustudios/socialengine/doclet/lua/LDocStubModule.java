package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

import java.util.ArrayList;
import java.util.List;

public class LDocStubModule {

    private final LDocStubHeader header;

    private final List<LDocStubMethod> methods = new ArrayList<>();

    private final ExposedModuleDefinition exposedModuleDefinition;

    public LDocStubModule(final ExposedModuleDefinition exposedModuleDefinition) {
        header = new LDocStubHeader(exposedModuleDefinition);
        this.exposedModuleDefinition = exposedModuleDefinition;
    }

    public LDocStubHeader getHeader() {
        return header;
    }

    public List<LDocStubMethod> getMethods() {
        return methods;
    }

    public LDocStubMethod addMethod(final String name) {
        final var method = new LDocStubMethod(exposedModuleDefinition, name);
        getMethods().add(method);
        return method;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LDocStubModule{");
        sb.append("header=").append(header);
        sb.append(", methods=").append(methods);
        sb.append(", exposedModuleDefinition=").append(exposedModuleDefinition);
        sb.append('}');
        return sb.toString();
    }

}
