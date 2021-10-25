package com.namazustudios.socialengine.doclet.visitor;

import com.namazustudios.socialengine.doclet.DocContext;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ReturnTree;

import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class DocCommentTags {

    /**
     * Gets all {@link com.sun.source.doctree.AuthorTree} values from the supplied {@link DocTree}.
     *
     * @param docTree the {@link DocTree}
     *
     * @return a {@link List<String>} naming all authors.
     */
    public static List<String> getAuthors(final DocContext docContext, final DocTree docTree) {

        final var names = docTree.accept(
            new DocTreeVisitorBuilder<List<DocTree>, List<DocTree>>()
                .withVisitErroneous((erroneousTree, aggregate) -> {
                    final var diagnostic = erroneousTree.getDiagnostic();
                    final var message = diagnostic.getMessage(docContext.getLocale());
                    final var name = diagnostic.getSource().getName();
                    final var line = diagnostic.getLineNumber();
                    final var column = diagnostic.getColumnNumber();
                    final var fullMessage = format("Malformed Javadoc %s: %s(%d, %d)", message, name, line, column);
                    docContext.getReporter().print(Diagnostic.Kind.WARNING, fullMessage);
                    return aggregate;
                })
                .withVisitAuthor((authorTree, aggregate) -> {
                    aggregate.addAll(authorTree.getName());
                    return aggregate;
                }).build(), new ArrayList<>());

        final var visitor = new DocTreeVisitorBuilder<String, Void>()
            .withVisitText((t, r) -> t.getBody())
            .build();

        return names
            .stream()
            .map(author -> author.accept(visitor, null))
            .collect(toList());

    }

    public static String getReturnComment(final DocContext docContext, final DocTree docTree) {

        final var returns = docTree.accept(
            new DocTreeVisitorBuilder<List<? extends DocTree>, Void>()
                .withVisitErroneous((erroneousTree, unused) -> {
                    final var diagnostic = erroneousTree.getDiagnostic();
                    final var message = diagnostic.getMessage(docContext.getLocale());
                    final var name = diagnostic.getSource().getName();
                    final var line = diagnostic.getLineNumber();
                    final var column = diagnostic.getColumnNumber();
                    final var fullMessage = format("Malformed Javadoc %s: %s(%d, %d)", message, name, line, column);
                    docContext.getReporter().print(Diagnostic.Kind.WARNING, fullMessage);
                    return null;
                })
                .withVisitReturn((returnTree, unused) -> returnTree.getDescription())
                .build(),null);

        return returns
            .stream()
            .map(dt -> DocCommentTags.getText(docContext, dt))
            .collect(joining());

    }

    public static String getText(final DocContext docContext, final DocTree docTree) {

        final var sb = new StringBuilder();

        docTree.accept(new DocTreeVisitorBuilder<Void, Void>()
            .withVisitSee((seeTree, docTrees) -> {
                final var see = format("(see: %s)", getText(docContext, seeTree));
                sb.append(see);
                return null;
            })
            .withVisitLink((linkTree, docTrees) -> {

                final var ref = linkTree.getReference();
                if (ref == null) return null;

                final var signature = ref.getSignature();
                if (signature == null) return null;

                sb.append(signature);
                return null;

            })
            .withVisitErroneous((erroneousTree, unused) -> {
                final var diagnostic = erroneousTree.getDiagnostic();
                final var message = diagnostic.getMessage(docContext.getLocale());
                final var name = diagnostic.getSource().getName();
                final var line = diagnostic.getLineNumber();
                final var column = diagnostic.getColumnNumber();
                final var fullMessage = format("Malformed Javadoc %s: %s(%d, %d)", message, name, line, column);
                docContext.getReporter().print(Diagnostic.Kind.WARNING, fullMessage);
                return null;
            })
            .withVisitText((t, unused) -> {
                sb.append(t.getBody());
                return null;
            }).build(), null);

        return sb.toString();

    }

}
