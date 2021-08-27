package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocRoot;
import com.namazustudios.socialengine.doclet.DocRootWriter;
import com.namazustudios.socialengine.doclet.metadata.ModuleDefinitionMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class LDocRootStubModule implements DocRoot {

    private final LDocStubModuleHeader header;

    private final List<LDocStubField> constants = new ArrayList<>();

    private final List<LDocStubMethod> methods = new ArrayList<>();

    private final ModuleDefinitionMetadata moduleDefinitionMetadata;

    public LDocRootStubModule(final ModuleDefinitionMetadata moduleDefinitionMetadata) {
        header = new LDocStubModuleHeader(moduleDefinitionMetadata);
        this.moduleDefinitionMetadata = moduleDefinitionMetadata;
    }

    public List<LDocStubField> getConstants() {
        return constants;
    }

    public LDocStubField addConstant(final String name) {

        final var inputCaseFormat = moduleDefinitionMetadata
            .getOutputCodeStyle()
            .getConstantCaseFormat();

        final var outputCaseFormat = moduleDefinitionMetadata
            .getOutputCodeStyle()
            .getConstantCaseFormat();

        final var field = new LDocStubField(inputCaseFormat.to(outputCaseFormat, name));
        constants.add(field);

        return field;
    }

    public LDocStubModuleHeader getHeader() {
        return header;
    }

    public List<LDocStubMethod> getMethods() {
        return methods;
    }

    public LDocStubMethod addMethod(final String name) {
        final var method = new LDocStubMethod(name, moduleDefinitionMetadata);
        getMethods().add(method);
        return method;
    }

    @Override
    public List<String> getRelativePath() {

        final var components = Stream
          .of(moduleDefinitionMetadata.getName().split("\\."))
          .collect(toList());

        final var file = format("%s.lua", components.remove(components.size() - 1));
        components.add(file);

        return components;

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
            writeConstantStub(table, c, writer);
        });

        if (!getConstants().isEmpty()) {
            writer.println();
        }

        getMethods().forEach(m -> {
            m.writeCommentHeader(writer);
            writeMethodStub(table, m, writer);
        });

        writer.printlnf("return %s", table);
        writer.println();

    }

    private void writeConstantStub(final String table,
                                   final LDocStubField constant,
                                   final DocRootWriter writer) {
        final var name = constant.getName();
        final var constantValue = nullToEmpty(constant.getConstantValue()).trim();

        try {
            final var val = Long.parseLong(constantValue);
            writer.printlnf("%s.%s=%d", table, name, val);
            return;
        } catch (NumberFormatException ex) {
            // Intentionally ignore exception
        }

        try {
            final var val = Double.parseDouble(constantValue);
            writer.printlnf("%s.%s=%f", table, name, val);
            return;
        } catch (NumberFormatException ex) {
            // Intentionally ignore exception
        }

        writer.printlnf("%s.%s=\"%s\"", table, name, constantValue);
        writer.println();

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

        try (var indent = writer.indent()) {
            writer.println("-- Stub ");
        }

        writer.println("end");
        writer.println();

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LDocStubModule{");
        sb.append("header=").append(header);
        sb.append(", methods=").append(methods);
        sb.append('}');
        return sb.toString();
    }

}
