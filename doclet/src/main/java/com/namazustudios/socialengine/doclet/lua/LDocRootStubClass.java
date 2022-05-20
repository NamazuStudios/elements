package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocRoot;
import com.namazustudios.socialengine.doclet.DocRootWriter;
import com.namazustudios.socialengine.doclet.metadata.ModuleDefinitionMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.List.copyOf;
import static java.util.stream.Collectors.toList;

public class LDocRootStubClass implements DocRoot {

    private final List<String> relativePath;

    private final LDocStubModuleHeader header;

    private final ModuleDefinitionMetadata moduleDefinitionMetadata;

    private final List<LDocStubField> constants = new ArrayList<>();

    private final List<LDocStubFunction> methods = new ArrayList<>();

    private final List<LDocStubFunction> constructors = new ArrayList<>();

    public LDocRootStubClass(final ModuleDefinitionMetadata moduleDefinitionMetadata) {

        final var relativePath = Stream
            .of(moduleDefinitionMetadata.getName().split("\\."))
            .collect(toList());

        final var file = format("%s.lua", relativePath.remove(relativePath.size() - 1));
        relativePath.add(file);

        header = new LDocStubModuleHeader(moduleDefinitionMetadata);
        header.appendMetadata(format("@module %s", moduleDefinitionMetadata.getName()));

        this.moduleDefinitionMetadata = moduleDefinitionMetadata;
        this.relativePath = copyOf(relativePath);

    }

    public LDocStubModuleHeader getHeader() {
        return header;
    }

    public List<LDocStubField> getConstants() {
        return constants;
    }

    public LDocStubField addConstant(final String name) {
        final var field = new LDocStubField(name);
        constants.add(field);
        return field;
    }

    public List<LDocStubFunction> getMethods() {
        return methods;
    }

    public LDocStubFunction addMethod(final String name) {
        final var method = new LDocStubFunction(name, moduleDefinitionMetadata);
        getMethods().add(method);
        return method;
    }

    public List<LDocStubFunction> getConstructors() {
        return constructors;
    }

    public LDocStubFunction addConstructor() {
        final var constructor = new LDocStubFunction("new", moduleDefinitionMetadata);
        constructors.add(constructor);
        return constructor;
    }

    @Override
    public List<String> getRelativePath() {
        return relativePath;
    }

    @Override
    public void write(final DocRootWriter writer) {

        getHeader().write(writer);
        writer.printCopyrightNotice("--");
        writer.println();

        final var table = moduleDefinitionMetadata.getName().replaceAll("\\.", "_");
        writer.printlnf("local %s = {}", table);
        writer.println();

        getConstants().forEach(c -> {
            c.writeCommentHeader(writer);
            c.writeConstantStub(writer, table);
        });

        if (!getConstants().isEmpty()) {
            writer.println();
        }

        if (!getConstructors().isEmpty()) {

            writer.println("--- Constructors");
            writer.println("-- @section constructors");
            writer.println();

            getConstructors().forEach(c -> {
                c.writeCommentHeader(writer);
                c.writeMethodStub(writer, table);
            });

            writer.println();
        }

        if (!getMethods().isEmpty()) {

            writer.println("--- Methods");
            writer.println("-- @section methods");
            writer.println();

            getMethods().forEach(m -> {
                m.writeCommentHeader(writer);
                m.writeMethodStub(writer, table);
            });

            writer.println();
        }

        writer.printlnf("return %s", table);

    }

}
