package dev.getelements.elements.doclet.lua;

import dev.getelements.elements.doclet.DocContext;
import dev.getelements.elements.doclet.DocProcessor;
import dev.getelements.elements.doclet.metadata.ModuleDefinitionMetadata;
import dev.getelements.elements.doclet.visitor.DocCommentTags;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposeEnum;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import com.sun.source.doctree.ParamTree;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.*;

import static dev.getelements.elements.doclet.metadata.TypeModifiers.isConstant;
import static dev.getelements.elements.doclet.metadata.TypeModifiers.isPublicModifier;
import static dev.getelements.elements.doclet.visitor.DocCommentTags.getAuthors;
import static dev.getelements.elements.doclet.visitor.DocCommentTags.getReturnComment;
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
            .map(ModuleDefinitionMetadata::fromAnnotation)
            .map(LDocRootStubModule::new)
            .collect(toList());

        for (final var stub : stubs) {

            final var comments = docContext
                .getDocTrees()
                .getDocCommentTree(typeElement);

            final var body = comments == null ? "" : comments.getFullBody()
                .stream()
                .map(docTree -> DocCommentTags.getText(docContext, docTree))
                .collect(joining());

            final var summary = comments == null ? "" : comments.getFirstSentence()
                .stream()
                .map(docTree -> DocCommentTags.getText(docContext, docTree))
                .collect(joining());

            if (comments != null) {
                comments
                    .getBlockTags()
                    .stream()
                    .map(docTree -> getAuthors(docContext, docTree))
                    .flatMap(Collection::stream)
                    .forEach(stub.getHeader()::addAuthor);
            }

            stub.getHeader().setSummary(summary);
            stub.getHeader().setDescription(body);

            docContext.getAuthors().forEach(stub.getHeader()::addAuthor);

            for (var enclosed : typeElement.getEnclosedElements()) {

                if (!exposedKinds.contains(enclosed.getKind())) continue;
                if (!docContext.getEnvironment().isIncluded(enclosed)) continue;

                switch (enclosed.getKind()) {
                    case FIELD:
                    case ENUM_CONSTANT:
                        if (isConstant(enclosed)) processField(stub, (VariableElement) enclosed);
                        break;
                    case METHOD:
                        if (isPublicModifier(enclosed)) processMethod(stub, (ExecutableElement) enclosed);
                        break;
                }
            }

        }

        return stubs;

    }

    private void processField(final LDocRootStubModule stub, final VariableElement enclosed) {

        final var comments = docContext.getDocTrees().getDocCommentTree(enclosed);

        final var name = enclosed.getSimpleName();

        final var summary = comments == null ? "" : comments.getFirstSentence()
            .stream()
            .map(docTree -> DocCommentTags.getText(docContext, docTree))
            .collect(joining());

        final var description = comments == null ? "" : comments.getBody()
            .stream()
            .map(docTree -> DocCommentTags.getText(docContext, docTree))
            .collect(joining());

        final var modifiers = enclosed.getModifiers();
        final var constantValue = enclosed.getConstantValue();
        final var typeDescription = LDocTypes.getTypeDescription(enclosed.asType());

        final var field = stub.addConstant(name.toString());
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

        final var summary = comments == null ? "" : comments
            .getFirstSentence()
            .stream()
            .map(docTree -> DocCommentTags.getText(docContext, docTree))
            .collect(joining());

        final var description = comments == null ? "" : comments
            .getBody()
            .stream()
            .map(docTree -> DocCommentTags.getText(docContext, docTree))
            .collect(joining());

        final var returnComment = comments == null ? "" : comments
            .getBlockTags()
            .stream()
            .filter(dt -> RETURN.equals(dt.getKind()))
            .map(docTree -> getReturnComment(docContext, docTree))
            .findFirst()
            .orElse("");

        final var method = stub.addFunction(name.toString());
        method.setSummary(summary);
        method.setDescription(description);

        final var docTreeParameters =
            comments == null ? Collections.<ParamTree>emptyList() : comments.getBlockTags()
                .stream()
                .filter(dt -> PARAM.equals(dt.getKind()))
                .map(ParamTree.class::cast)
                .collect(toList());

        method.addReturnValue(returnTypeDescription, returnComment);
        processParameters(enclosed.getParameters(), method, docTreeParameters);

    }

    private void processParameters(final List<? extends VariableElement> parameters,
                                   final LDocStubFunction method,
                                   final List<ParamTree> docTreeParameters) {
        for (var param : parameters) {

            final var name = param.getSimpleName();
            final var type = LDocTypes.getTypeDescription(param.asType());
            final var comment = docTreeParameters
                .stream()
                .filter(pt -> name.equals(pt.getName().getName()))
                .map(ParamTree::getDescription)
                .flatMap(Collection::stream)
                .map(docTree -> DocCommentTags.getText(docContext, docTree))
                .collect(joining());

            method.addParameter(name.toString(), type, comment);

        }
    }

}
