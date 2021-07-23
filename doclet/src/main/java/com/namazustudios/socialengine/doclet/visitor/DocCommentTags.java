package com.namazustudios.socialengine.doclet.visitor;

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.ReturnTree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public static List<String> getAuthors(final DocTree docTree) {

        final var names = docTree.accept(
            new DocTreeVisitorBuilder<List<DocTree>, List<DocTree>>()
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

    public static String getReturnComment(final DocTree docTree) {

        final var returns = docTree.accept(
            new DocTreeVisitorBuilder<List<? extends DocTree>, Void>()
                .withVisitReturn((returnTree, unused) -> returnTree.getDescription())
                .build(),null);

        return returns
            .stream()
            .map(DocCommentTags::getText)
            .collect(joining());

    }

    public static String getText(final DocTree docTree) {

        final var sb = new StringBuilder();

        docTree.accept(new DocTreeVisitorBuilder<Void, Void>()
            .withVisitText((t, unused) -> {
                sb.append(t.getBody());
                return null;
            }).build(), null);

        return sb.toString();

    }

}
