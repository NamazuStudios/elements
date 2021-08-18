package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocRoot;
import com.namazustudios.socialengine.doclet.DocRootWriter;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

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
    public List<String> getRelativePath() {

        final var components = Stream
          .of(moduleDefinition.value().split("\\."))
          .collect(toList());

        final var file = format("%s.lua", components.remove(components.size() - 1));
        components.add(file);

        return components;

    }

    @Override
    public void write(final DocRootWriter writer) {

        getHeader().write(writer);

        final var table = moduleDefinition.value().replaceAll("\\.", "_");

        writer.printlnf("local %s = {}", table);

        getMethods().forEach(m -> {
            m.writeCommentHeader(writer);
            writeMethodStub(table, m, writer);
        });

        writer.printlnf("return %s", table);

    }

    private void writeMethodStub(final String table,
                                 final LDocStubMethod method,
                                 final DocRootWriter writer) {

        final var name = method.getName();

        final var params = method.getParameters()
            .stream()
            .map(LDocParameter::getName)
            .collect(joining(","));

        writer.printlnf("function %s.%s(%s)", table, name, params);
        writer.println("-- Stub ");
        writer.println("end");

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
