package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocRoot;
import com.namazustudios.socialengine.doclet.DocRootWriter;
import com.namazustudios.socialengine.doclet.metadata.ModuleDefinitionMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.List.copyOf;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class LDocRootStubClass implements DocRoot {

    private final List<String> relativePath;

    private final LDocStubClassHeader header;

    private final ModuleDefinitionMetadata moduleDefinitionMetadata;

    private final List<LDocStubField> constants = new ArrayList<>();

    private final List<LDocStubMethod> methods = new ArrayList<>();

    private final List<LDocConstructor> constructors = new ArrayList<>();

    public LDocRootStubClass(final ModuleDefinitionMetadata moduleDefinitionMetadata) {

        final var relativePath = Stream
            .of(moduleDefinitionMetadata.getName().toString().split("\\."))
            .collect(toList());

        final var file = format("%s.moon", relativePath.remove(relativePath.size() - 1));
        relativePath.add(file);

        header = new LDocStubClassHeader(moduleDefinitionMetadata);

        this.moduleDefinitionMetadata = moduleDefinitionMetadata;
        this.relativePath = copyOf(relativePath);

    }

    public LDocStubClassHeader getHeader() {
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

    public List<LDocStubMethod> getMethods() {
        return methods;
    }

    public LDocStubMethod addMethod(final String name) {
        final var method = new LDocStubMethod(name, moduleDefinitionMetadata);
        getMethods().add(method);
        return method;
    }

    public List<LDocConstructor> getConstructors() {
        return constructors;
    }

    public LDocConstructor addConstructor() {
        final var variant = format("(Variant #%s)", constructors.size() + 1);
        final var constructor = new LDocConstructor(variant);
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

        writer.printlnf("class %s", getHeader().getName());

        try (var indent = writer.indent()) {

            final var ctors = getConstructors();

            switch (ctors.size()) {
                case 0:
                    writeDefaultConstructor(writer);
                    break;
                case 1:
                    writeSingleConstructor(writer);
                    break;
                default:
                    writeAggregateConstructor(writer);
                    break;
            }

            getMethods().forEach(m -> {
                m.writeCommentHeader(writer);
                writeMethodStub(m, writer);
            });

        }

    }

    private void writeDefaultConstructor(final DocRootWriter writer) {

        writer.println("--- Default Constructor");
        writer.println("--");
        writer.println("new: =>");

        try (var indent = writer.indent()) {
            writer.println("-- Stub ");
        }

    }

    private void writeSingleConstructor(final DocRootWriter writer) {
        final var ctor = getConstructors().get(0);
        ctor.writeSingleConstructor(writer);
    }

    private void writeAggregateConstructor(final DocRootWriter writer) {
        getConstructors().forEach(c -> c.writeConstructorVariant(writer));
        getConstructors().forEach(c -> c.writeConstructorParameters(writer));
    }

    private void writeMethodStub(final LDocStubMethod method,
                                 final DocRootWriter writer) {

        final var name = method.getName();

        final var params = method.getParameters()
            .stream()
            .map(LDocParameter::getName)
            .collect(joining(","));

        writer.printlnf("%s: (%s) =>", name, params);

        try (var indent = writer.indent()) {
            writer.println("-- Stub ");
        }

        writer.println();

    }

}

