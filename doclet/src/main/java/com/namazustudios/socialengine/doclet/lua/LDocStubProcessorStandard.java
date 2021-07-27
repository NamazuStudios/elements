package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocContext;
import com.namazustudios.socialengine.doclet.visitor.DocCommentTags;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;

public class LDocStubProcessorStandard implements LDocProcessor<LDocStubClass> {

    private final DocContext docContext;

    private final TypeElement typeElement;

    public LDocStubProcessorStandard(final DocContext docContext, final TypeElement typeElement) {
        this.docContext = docContext;
        this.typeElement = typeElement;
    }

    @Override
    public List<LDocStubClass> process() {
        final var classes = new ArrayList<LDocStubClass>();
        process(classes, typeElement);
        return classes;
    }

    private void process(final ArrayList<LDocStubClass> classes, final TypeElement typeElement) {

        final var docTree = docContext.getDocTrees().getDocCommentTree(typeElement);
        final var stubClass = new LDocStubClass(typeElement.getQualifiedName().toString());

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

        for (var enclosed : typeElement.getEnclosedElements()) {

            final var modifiers = enclosed.getModifiers();

            if (modifiers.contains(PUBLIC) || modifiers.contains(PROTECTED)) {
                switch (enclosed.getKind()) {
                    case FIELD:
                        processField(stubClass, (VariableElement) enclosed);
                        break;
                    case ENUM_CONSTANT:
                        processField(stubClass, (VariableElement) enclosed);
                        break;
                    case METHOD:
                        processExecutable(stubClass, (ExecutableElement) enclosed);
                        break;
                }
            }

        }

    }

    private void processField(final LDocStubClass stubClass, final VariableElement enclosed) {
        final var docTree = docContext.getDocTrees().getDocCommentTree(enclosed);

        final var name = enclosed.getSimpleName();
        final var method = stubClass.addMethod(name.toString());

        final var summary = docTree.getFirstSentence()
            .stream()
            .map(DocCommentTags::getText)
            .collect(joining());

        final var description = docTree.getFullBody()
            .stream()
            .map(DocCommentTags::getText)
            .collect(joining());

    }


    private void processExecutable(final LDocStubClass stubClass, final ExecutableElement enclosed) {
    }

}
