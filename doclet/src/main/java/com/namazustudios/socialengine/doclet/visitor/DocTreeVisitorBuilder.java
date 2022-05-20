package com.namazustudios.socialengine.doclet.visitor;

import com.sun.source.doctree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class DocTreeVisitorBuilder<R, P> {

    private final List<Consumer<FunctionalDocTreeVisitor<R,P>>> operations;

    public DocTreeVisitorBuilder() {
        operations = new ArrayList<>();
    }

    private DocTreeVisitorBuilder(final List<Consumer<FunctionalDocTreeVisitor<R, P>>> operations) {
        this.operations = operations;
    }

    public DocTreeVisitorBuilder<R, P> withVisitAttribute(final BiFunction<AttributeTree, P, R> visitAttribute) {
        operations.add(v -> v.setVisitAttribute(visitAttribute));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitAuthor(final BiFunction<AuthorTree, P, R> visitAuthor) {
        operations.add(v -> v.setVisitAuthor(visitAuthor));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitComment(final BiFunction<CommentTree, P, R> visitComment) {
        operations.add(v -> v.setVisitComment(visitComment));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitDeprecated(final BiFunction<DeprecatedTree, P, R> visitDeprecated) {
        operations.add(v -> v.setVisitDeprecated(visitDeprecated));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitDocComment(final BiFunction<DocCommentTree, P, R> visitDocComment) {
        operations.add(v -> v.setVisitDocComment(visitDocComment));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitDocRoot(final BiFunction<DocRootTree, P, R> visitDocRoot) {
        operations.add(v -> v.setVisitDocRoot(visitDocRoot));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitEndElement(final BiFunction<EndElementTree, P, R> visitEndElement) {
        operations.add(v -> v.setVisitEndElement(visitEndElement));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitEntity(final BiFunction<EntityTree, P, R> visitEntity) {
        operations.add(v -> v.setVisitEntity(visitEntity));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitErroneous(final BiFunction<ErroneousTree, P, R> visitErroneous) {
        operations.add(v -> v.setVisitErroneous(visitErroneous));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitIdentifier(final BiFunction<IdentifierTree, P, R> visitIdentifier) {
        operations.add(v -> v.setVisitIdentifier(visitIdentifier));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitInheritDoc(final BiFunction<InheritDocTree, P, R> visitInheritDoc) {
        operations.add(v -> v.setVisitInheritDoc(visitInheritDoc));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitLink(final BiFunction<LinkTree, P, R> visitLink) {
        operations.add(v -> v.setVisitLink(visitLink));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitLiteral(final BiFunction<LiteralTree, P, R> visitLiteral) {
        operations.add(v -> v.setVisitLiteral(visitLiteral));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitParam(final BiFunction<ParamTree, P, R> visitParam) {
        operations.add(v -> v.setVisitParam(visitParam));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitReference(final BiFunction<ReferenceTree, P, R> visitReference) {
        operations.add(v -> v.setVisitReference(visitReference));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitReturn(final BiFunction<ReturnTree, P, R> visitReturn) {
        operations.add(v -> v.setVisitReturn(visitReturn));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitSee(final BiFunction<SeeTree, P, R> visitSee) {
        operations.add(v -> v.setVisitSee(visitSee));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitSerial(final BiFunction<SerialTree, P, R> visitSerial) {
        operations.add(v -> v.setVisitSerial(visitSerial));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitSerialData(final BiFunction<SerialDataTree, P, R> visitSerialData) {
        operations.add(v -> v.setVisitSerialData(visitSerialData));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitSerialField(final BiFunction<SerialFieldTree, P, R> visitSerialField) {
        operations.add(v -> v.setVisitSerialField(visitSerialField));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitSince(final BiFunction<SinceTree, P, R> visitSince) {
        operations.add(v -> v.setVisitSince(visitSince));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitStartElement(final BiFunction<StartElementTree, P, R> visitStartElement) {
        operations.add(v -> v.setVisitStartElement(visitStartElement));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitText(final BiFunction<TextTree, P, R> visitText) {
        operations.add(v -> v.setVisitText(visitText));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitThrows(final BiFunction<ThrowsTree, P, R> visitThrows) {
        operations.add(v -> v.setVisitThrows(visitThrows));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitUnknownBlockTag(final BiFunction<UnknownBlockTagTree, P, R> visitUnknownBlockTag) {
        operations.add(v -> v.setVisitUnknownBlockTag(visitUnknownBlockTag));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitUnknownInlineTag(final BiFunction<UnknownInlineTagTree, P, R> visitUnknownInlineTag) {
        operations.add(v -> v.setVisitUnknownInlineTag(visitUnknownInlineTag));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitValue(final BiFunction<ValueTree, P, R> visitValue) {
        operations.add(v -> v.setVisitValue(visitValue));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitVersion(final BiFunction<VersionTree, P, R> visitVersion) {
        operations.add(v -> v.setVisitVersion(visitVersion));
        return this;
    }

    public DocTreeVisitorBuilder<R, P> withVisitOther(final BiFunction<DocTree, P, R> visitOther) {
        operations.add(v -> v.setVisitOther(visitOther));
        return this;
    }

    public DocTreeVisitor<R,P> build() {
        final var visitor = new FunctionalDocTreeVisitor<R, P>();
        operations.forEach(c -> c.accept(visitor));
        return visitor;
    }

}
