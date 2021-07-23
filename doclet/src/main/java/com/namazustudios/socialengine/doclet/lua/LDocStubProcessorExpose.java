package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocContext;
import com.namazustudios.socialengine.doclet.visitor.DocCommentTags;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.sun.source.doctree.ParamTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.sun.source.doctree.DocTree.Kind.PARAM;
import static com.sun.source.doctree.DocTree.Kind.RETURN;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.METHOD;

public class LDocStubProcessorExpose implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(LDocStubProcessorExpose.class);

    private final Expose expose;

    private final DocContext docContext;

    private final TypeElement typeElement;

    private List<LDocStubModule> stubs = emptyList();

    public LDocStubProcessorExpose(final DocContext docContext,
                                   final TypeElement typeElement,
                                   final Expose expose) {
        this.docContext = docContext;
        this.typeElement = typeElement;
        this.expose = expose;
    }

    @Override
    public void close() throws IOException {
        // TODO Write to Disk.
    }

    public List<LDocStubModule> process() {

        final var stubs = Arrays.stream(expose.value())
            .map(LDocStubModule::new)
            .collect(toList());

        for (final var stub : stubs) {

            final var comments = docContext
                .getDocTrees()
                .getDocCommentTree(typeElement);

            final var body = comments.getBody()
                .stream()
                .map(DocCommentTags::getText)
                .collect(Collectors.joining());

            comments
                .getBlockTags()
                .stream()
                .map(DocCommentTags::getAuthors)
                .flatMap(Collection::stream)
                .forEach(stub.getHeader()::addAuthor);

            stub.getHeader().setDescription(body);

            for (var enclosed : typeElement.getEnclosedElements()) {
                if (METHOD.equals(enclosed.getKind())) processMethod(stub, (ExecutableElement) enclosed);
            }

        }

        return stubs;

    }

    private void processMethod(final LDocStubModule stub, final ExecutableElement enclosed) {

        final var comments = docContext.getDocTrees().getDocCommentTree(enclosed);

        final var name = enclosed.getSimpleName();
        final var returnType = enclosed.getReturnType();
        final var returnTypeDescription = LDocTypes.getTypeDescription(returnType);

        final var returnComment = comments
            .getBlockTags()
            .stream()
            .filter(dt -> RETURN.equals(dt.getKind()))
            .map(DocCommentTags::getReturnComment)
            .findFirst()
            .orElse(null);

        final var method = stub.addMethod(name.toString());

        final var docTreeParameters = comments.getBlockTags()
            .stream()
            .filter(dt -> PARAM.equals(dt.getKind()))
            .map(ParamTree.class::cast)
            .collect(toList());

        method.addReturnValue(returnTypeDescription, returnComment);
        processParameters(enclosed.getParameters(), method, docTreeParameters);

    }

    private void processParameters(final List<? extends VariableElement> parameters,
                                   final LDocStubMethod method,
                                   final List<ParamTree> docTreeParameters) {
        for (var param : parameters) {

            final var name = param.getSimpleName();
            final var type = LDocTypes.getTypeDescription(param.asType());
            final var comment = docTreeParameters
                .stream()
                .filter(pt -> name.equals(pt.getName().getName()))
                .map(ParamTree::getDescription)
                .flatMap(Collection::stream)
                .map(DocCommentTags::getText)
                .collect(joining());

            method.addParameter(name.toString(), type, comment);

        }
    }

}
