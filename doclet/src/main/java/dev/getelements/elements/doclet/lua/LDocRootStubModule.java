package dev.getelements.elements.doclet.lua;

import dev.getelements.elements.doclet.DocRoot;
import dev.getelements.elements.doclet.DocRootWriter;
import dev.getelements.elements.doclet.metadata.ModuleDefinitionMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class LDocRootStubModule implements DocRoot {

    private final LDocStubModuleHeader header;

    private final List<LDocStubField> constants = new ArrayList<>();

    private final List<LDocStubFunction> functions = new ArrayList<>();

    private final ModuleDefinitionMetadata moduleDefinitionMetadata;

    public LDocRootStubModule(final ModuleDefinitionMetadata moduleDefinitionMetadata) {
        header = new LDocStubModuleHeader(moduleDefinitionMetadata);
        header.appendMetadata(format("@module %s", moduleDefinitionMetadata.getName()));
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

    public List<LDocStubFunction> getFunctions() {
        return functions;
    }

    public LDocStubFunction addFunction(final String name) {
        final var function = new LDocStubFunction(name, moduleDefinitionMetadata);
        getFunctions().add(function);
        return function;
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
            c.writeConstantStub(writer, table);
        });

        if (!getConstants().isEmpty()) {
            writer.println();
        }

        getFunctions().forEach(m -> {
            m.writeCommentHeader(writer);
            m.writeFunctionStub(writer, table);
        });

        writer.printlnf("return %s", table);
        writer.println();

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LDocStubModule{");
        sb.append("header=").append(header);
        sb.append(", functions=").append(functions);
        sb.append('}');
        return sb.toString();
    }

}
