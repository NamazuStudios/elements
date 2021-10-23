package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocContext;
import com.namazustudios.socialengine.doclet.DocProcessor;
import com.namazustudios.socialengine.doclet.visitor.FunctionalElementVisitor;
import com.sun.source.doctree.ParamTree;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.namazustudios.socialengine.doclet.metadata.ModuleDefinitionMetadata.fromJavaClassName;
import static com.namazustudios.socialengine.doclet.metadata.TypeModifiers.isConstant;
import static com.namazustudios.socialengine.doclet.metadata.TypeModifiers.isPublic;
import static com.namazustudios.socialengine.doclet.visitor.DocCommentTags.getReturnComment;
import static com.namazustudios.socialengine.doclet.visitor.DocCommentTags.getText;
import static com.sun.source.doctree.DocTree.Kind.PARAM;
import static com.sun.source.doctree.DocTree.Kind.RETURN;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;

public class LDocStubProcessorStandard implements DocProcessor<LDocRootStubClass> {

    private final DocContext docContext;

    private final TypeElement typeElement;

    public LDocStubProcessorStandard(final DocContext docContext, final TypeElement typeElement) {
        this.docContext = docContext;
        this.typeElement = typeElement;
    }

    @Override
    public List<LDocRootStubClass> process() {
        final var classes = new ArrayList<LDocRootStubClass>();
        process(classes, typeElement);
        return classes;
    }

    private void process(final ArrayList<LDocRootStubClass> classes, final TypeElement typeElement) {

        final var docTree = docContext.getDocTrees().getDocCommentTree(typeElement);

        final var deprecated = typeElement.getAnnotation(Deprecated.class);
        final var moduleDefinitionMetadata = fromJavaClassName(typeElement.getQualifiedName().toString(), deprecated);

        final var stubClass = new LDocRootStubClass(moduleDefinitionMetadata);

        docContext.getAuthors().forEach(stubClass.getHeader()::addAuthor);

        final var summary = docTree == null ? "" : docTree.getFirstSentence()
            .stream()
            .map(dt -> getText(docContext, dt))
            .collect(joining());

        final var description = docTree == null ? "" : docTree.getBody()
            .stream()
            .map(dt -> getText(docContext, dt))
            .collect(joining());

        final var header = stubClass.getHeader();

        header.setSummary(summary);
        header.setDescription(description);
        header.prependMetadata(format("@usage java.require \"%s\"", typeElement.getQualifiedName()));

        for (var enclosed : typeElement.getEnclosedElements()) {

            final var modifiers = enclosed.getModifiers();

            if (modifiers.contains(PUBLIC) || modifiers.contains(PROTECTED)) {
                switch (enclosed.getKind()) {
                    case CLASS:
                        if (isPublic(enclosed)) process(classes, (TypeElement) enclosed);
                        break;
                    case FIELD:
                    case ENUM_CONSTANT:
                        if (isConstant(enclosed)) processField(stubClass, (VariableElement) enclosed);
                        break;
                    case METHOD:
                        if (isPublic(enclosed)) processMethod(stubClass, (ExecutableElement) enclosed);
                        break;
                    case CONSTRUCTOR:
                        if (isPublic(enclosed)) processConstructor(stubClass, (ExecutableElement) enclosed);
                        break;
                }
            }

        }

        classes.add(stubClass);

    }

    private void processField(final LDocRootStubClass stub, final VariableElement enclosed) {

        final var docTree = docContext.getDocTrees().getDocCommentTree(enclosed);

        final var name = enclosed.getSimpleName();

        final var summary = docTree == null ? "" : docTree.getFirstSentence()
            .stream()
            .map(dt -> getText(docContext, dt))
            .collect(joining());

        final var description = docTree == null ? "" : docTree.getFullBody()
            .stream()
            .map(dt -> getText(docContext, dt))
            .collect(joining());

        final var constantValue = enclosed.getConstantValue();
        final var typeDescription = LDocTypes.getTypeDescription(enclosed.asType());

        final var field = stub.addConstant(name.toString());
        field.setType(typeDescription);
        field.setSummary(summary);
        field.setDescription(description);
        field.setConstantValue(constantValue);

    }

    private void processMethod(final LDocRootStubClass stub, final ExecutableElement enclosed) {

        final var comments = docContext.getDocTrees().getDocCommentTree(enclosed);

        final var name = enclosed.getSimpleName();
        final var returnType = enclosed.getReturnType();
        final var returnTypeDescription = LDocTypes.getTypeDescription(returnType);

        final var summary = comments == null ? "" : comments.getFirstSentence()
            .stream()
            .map(dt -> getText(docContext, dt))
            .collect(joining());

        final var description = comments == null ? "" : comments.getBody()
            .stream()
            .map(dt -> getText(docContext, dt))
            .collect(joining());

        final var returnComment = comments == null ? "" : comments
            .getBlockTags()
            .stream()
            .filter(dt -> RETURN.equals(dt.getKind()))
            .map(docTree -> getReturnComment(docContext, docTree))
            .findFirst()
            .orElse("");

        final var method = stub.addMethod(name.toString());
        method.setSummary(summary);
        method.setDescription(description);

        final var docTreeParameters = comments == null
            ? Collections.<ParamTree>emptyList()
            : comments.getBlockTags()
                .stream()
                .filter(dt -> PARAM.equals(dt.getKind()))
                .map(ParamTree.class::cast)
                .collect(toList());

        method.addReturnValue(returnTypeDescription, returnComment);
        processParameters(enclosed.getParameters(), method, docTreeParameters);

    }

    private void processConstructor(final LDocRootStubClass stub,
                                    final ExecutableElement enclosed) {

        final var ctor = stub.addConstructor();
        final var comments = docContext.getDocTrees().getDocCommentTree(enclosed);

        final var docTreeParameters = comments == null
            ? Collections.<ParamTree>emptyList()
            : comments.getBlockTags()
                .stream()
                .filter(dt -> PARAM.equals(dt.getKind()))
                .map(ParamTree.class::cast)
                .collect(toList());

        processParameters(enclosed.getParameters(), ctor, docTreeParameters);

    }

    private void processParameters(final List<? extends VariableElement> parameters,
                                   final LDocStubFunction ctor,
                                   final List<ParamTree> docTreeParameters) {
        for (var param : parameters) {

            final var name = param.getSimpleName();
            final var type = LDocTypes.getTypeDescription(param.asType());
            final var comment = docTreeParameters
                    .stream()
                    .filter(pt -> name.equals(pt.getName().getName()))
                    .map(ParamTree::getDescription)
                    .flatMap(Collection::stream)
                    .map(dt -> getText(docContext, dt))
                    .collect(joining());

            ctor.addParameter(name.toString(), type, comment);

        }
    }

}
