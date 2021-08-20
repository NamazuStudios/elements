package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocContext;
import com.namazustudios.socialengine.doclet.DocProcessor;
import com.namazustudios.socialengine.doclet.visitor.DocCommentTags;
import com.sun.source.doctree.ParamTree;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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

        final var relative = Stream
            .of(typeElement.getQualifiedName().toString().split("\\."))
            .collect(toList());

        final var file = format("%s.moon", relative.remove(relative.size() - 1));
        relative.add(file);

        final var stubClass = new LDocRootStubClass(typeElement.getQualifiedName().toString(), relative);

        final var summary = docTree.getFirstSentence()
            .stream()
            .map(DocCommentTags::getText)
            .collect(joining());

        final var description = docTree.getFullBody()
            .stream()
            .map(DocCommentTags::getText)
            .collect(joining());

        final var header = stubClass.getHeader();

        header.setSummary(summary);
        header.setDescription(description);

        header.addExtraDescriptionFormat(
            "Note: Instantiate with java.require\"%s\"",
            typeElement.getQualifiedName());

        for (var enclosed : typeElement.getEnclosedElements()) {

            final var modifiers = enclosed.getModifiers();

            if (modifiers.contains(PUBLIC) || modifiers.contains(PROTECTED)) {
                switch (enclosed.getKind()) {
                    case FIELD:
                    case ENUM_CONSTANT:
                        processField(stubClass, (VariableElement) enclosed);
                        break;
                    case METHOD:
                        processMethod(stubClass, (ExecutableElement) enclosed);
                        break;
                    case CONSTRUCTOR:
                        processConstructor(stubClass, (ExecutableElement) enclosed);
                        break;
                }
            }

        }

    }

    private void processField(final LDocRootStubClass stub, final VariableElement enclosed) {

        final var docTree = docContext.getDocTrees().getDocCommentTree(enclosed);

        final var name = enclosed.getSimpleName();

        final var summary = docTree.getFirstSentence()
            .stream()
            .map(DocCommentTags::getText)
            .collect(joining());

        final var description = docTree.getFullBody()
            .stream()
            .map(DocCommentTags::getText)
            .collect(joining());

        final var constantValue = enclosed.getConstantValue();
        final var typeDescription = LDocTypes.getTypeDescription(enclosed.asType());

        final var field = stub.getHeader().addField(name.toString());
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

        final var returnComment = comments == null ? "" : comments
                .getBlockTags()
                .stream()
                .filter(dt -> RETURN.equals(dt.getKind()))
                .map(DocCommentTags::getReturnComment)
                .findFirst()
                .orElse("");

        final var method = stub.addMethod(name.toString());

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
                                   final LDocConstructor ctor,
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

            ctor.addParameter(name.toString(), type, comment);

        }
    }

}
