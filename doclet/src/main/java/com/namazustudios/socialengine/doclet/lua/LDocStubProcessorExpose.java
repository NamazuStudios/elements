package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocContext;
import com.namazustudios.socialengine.doclet.DocProcessor;
import com.namazustudios.socialengine.doclet.visitor.DocCommentTags;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposeEnum;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;
import com.sun.source.doctree.ParamTree;

import javax.lang.model.element.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.sun.source.doctree.DocTree.Kind.PARAM;
import static com.sun.source.doctree.DocTree.Kind.RETURN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.*;

public class LDocStubProcessorExpose implements DocProcessor<LDocRootStubModule> {

    private final ModuleDefinition[] moduleDefinitions;

    private final DocContext docContext;

    private final TypeElement typeElement;

    private final Set<ElementKind> exposedKinds;

    public LDocStubProcessorExpose(final DocContext docContext,
                                   final TypeElement typeElement,
                                   final Expose expose) {
        this.docContext = docContext;
        this.typeElement = typeElement;
        this.moduleDefinitions = expose.value();
        this.exposedKinds = new HashSet<>(asList(METHOD, FIELD));
    }

    public LDocStubProcessorExpose(final DocContext docContext,
                                   final TypeElement typeElement,
                                   final ExposeEnum exposeEnum) {
        this.docContext = docContext;
        this.typeElement = typeElement;
        this.moduleDefinitions = exposeEnum.value();
        this.exposedKinds = new HashSet<>(singletonList(ENUM_CONSTANT));
    }

    public List<LDocRootStubModule> process() {

        final var stubs = Arrays.stream(moduleDefinitions)
            .map(LDocRootStubModule::new)
            .collect(toList());

        for (final var stub : stubs) {

            final var comments = docContext
                .getDocTrees()
                .getDocCommentTree(typeElement);

            final var body = comments.getFullBody()
                .stream()
                .map(DocCommentTags::getText)
                .collect(Collectors.joining());

            final var summary = comments.getFirstSentence()
                .stream()
                .map(DocCommentTags::getText)
                .collect(Collectors.joining());

            comments
                .getBlockTags()
                .stream()
                .map(DocCommentTags::getAuthors)
                .flatMap(Collection::stream)
                .forEach(stub.getHeader()::addAuthor);

            stub.getHeader().setSummary(summary);
            stub.getHeader().setDescription(body);

            for (var enclosed : typeElement.getEnclosedElements()) {

                if (!exposedKinds.contains(enclosed.getKind())) continue;
                if (!docContext.getEnvironment().isIncluded(enclosed)) continue;

                switch (enclosed.getKind()) {
                    case FIELD:
                    case ENUM_CONSTANT:
                        processField(stub, (VariableElement) enclosed);
                        break;
                    case METHOD:
                        processMethod(stub, (ExecutableElement) enclosed);
                        break;
                }
            }

        }

        return stubs;

    }

    private void processField(final LDocRootStubModule stub, final VariableElement enclosed) {

        final var comments = docContext.getDocTrees().getDocCommentTree(enclosed);

        final var name = enclosed.getSimpleName();

        final var summary = comments.getFirstSentence()
            .stream()
            .map(DocCommentTags::getText)
            .collect(Collectors.joining());

        final var description = comments.getBody()
            .stream()
            .map(DocCommentTags::getText)
            .collect(Collectors.joining());

        final var modifiers = enclosed.getModifiers();
        final var constantValue = enclosed.getConstantValue();
        final var typeDescription = LDocTypes.getTypeDescription(enclosed.asType());

        final LDocStubField field;

        if (modifiers.contains(Modifier.STATIC) && modifiers.contains(Modifier.FINAL)) {
            field = stub.getHeader().addField(name.toString(), UPPER_UNDERSCORE);
        } else {
            field = stub.getHeader().addField(name.toString(), LOWER_CAMEL);
        }

        field.setType(typeDescription);
        field.setSummary(summary);
        field.setDescription(description);
        field.setConstantValue(constantValue);

    }

    private void processMethod(final LDocRootStubModule stub, final ExecutableElement enclosed) {

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
            .orElse("");

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
