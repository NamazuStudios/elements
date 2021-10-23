package com.namazustudios.socialengine.doclet.visitor;

import com.sun.source.doctree.*;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class FunctionalDocTreeVisitor<R, P> implements DocTreeVisitor<R, P> {

    private Supplier<R> defaultResultSupplier = () -> null;

    private <ElementT extends DocTree> BiFunction<ElementT, P, R> pass() {
        return (r, p) -> defaultResultSupplier.get();
    }

    private BiFunction<AttributeTree, P, R> visitAttribute = pass();

    private BiFunction<AuthorTree, P, R> visitAuthor = pass();

    private BiFunction<CommentTree, P, R> visitComment = pass();

    private BiFunction<DeprecatedTree, P, R> visitDeprecated = pass();

    private BiFunction<DocCommentTree, P, R> visitDocComment = pass();

    private BiFunction<DocRootTree, P, R> visitDocRoot = pass();

    private BiFunction<EndElementTree, P, R> visitEndElement = pass();

    private BiFunction<EntityTree, P, R> visitEntity = pass();

    private BiFunction<ErroneousTree, P, R> visitErroneous = pass();

    private BiFunction<IdentifierTree, P, R> visitIdentifier = pass();

    private BiFunction<InheritDocTree, P, R> visitInheritDoc = pass();

    private BiFunction<LinkTree, P, R> visitLink = pass();

    private BiFunction<LiteralTree, P, R> visitLiteral = pass();

    private BiFunction<ParamTree, P, R> visitParam = pass();

    private BiFunction<ReferenceTree, P, R> visitReference = pass();

    private BiFunction<ReturnTree, P, R> visitReturn = pass();

    private BiFunction<SeeTree, P, R> visitSee = pass();

    private BiFunction<SerialTree, P, R> visitSerial = pass();

    private BiFunction<SerialDataTree, P, R> visitSerialData = pass();

    private BiFunction<SerialFieldTree, P, R> visitSerialField = pass();

    private BiFunction<SinceTree, P, R> visitSince = pass();

    private BiFunction<StartElementTree, P, R> visitStartElement = pass();

    private BiFunction<TextTree, P, R> visitText = pass();

    private BiFunction<ThrowsTree, P, R> visitThrows = pass();

    private BiFunction<UnknownBlockTagTree, P, R> visitUnknownBlockTag = pass();

    private BiFunction<UnknownInlineTagTree, P, R> visitUnknownInlineTag = pass();

    private BiFunction<ValueTree, P, R> visitValue = pass();

    private BiFunction<VersionTree, P, R> visitVersion = pass();

    private BiFunction<DocTree, P, R> visitOther = pass();

    @Override
    public R visitAttribute(AttributeTree node, P p) {
        return visitAttribute.apply(node, p);
    }

    @Override
    public R visitAuthor(AuthorTree node, P p) {
        return visitAuthor.apply(node, p);
    }

    @Override
    public R visitComment(CommentTree node, P p) {
        return visitComment.apply(node, p);
    }

    @Override
    public R visitDeprecated(DeprecatedTree node, P p) {
        return visitDeprecated.apply(node, p);
    }

    @Override
    public R visitDocComment(DocCommentTree node, P p) {
        return visitDocComment.apply(node, p);
    }

    @Override
    public R visitDocRoot(DocRootTree node, P p) {
        return visitDocRoot.apply(node, p);
    }

    @Override
    public R visitEndElement(EndElementTree node, P p) {
        return visitEndElement.apply(node, p);
    }

    @Override
    public R visitEntity(EntityTree node, P p) {
        return visitEntity.apply(node, p);
    }

    @Override
    public R visitErroneous(ErroneousTree node, P p) {
        return visitErroneous.apply(node, p);
    }

    @Override
    public R visitIdentifier(IdentifierTree node, P p) {
        return visitIdentifier.apply(node, p);
    }

    @Override
    public R visitInheritDoc(InheritDocTree node, P p) {
        return visitInheritDoc.apply(node, p);
    }

    @Override
    public R visitLink(LinkTree node, P p) {
        return visitLink.apply(node, p);
    }

    @Override
    public R visitLiteral(LiteralTree node, P p) {
        return visitLiteral.apply(node, p);
    }

    @Override
    public R visitParam(ParamTree node, P p) {
        return visitParam.apply(node, p);
    }

    @Override
    public R visitReference(ReferenceTree node, P p) {
        return visitReference.apply(node, p);
    }

    @Override
    public R visitReturn(ReturnTree node, P p) {
        return visitReturn.apply(node, p);
    }

    @Override
    public R visitSee(SeeTree node, P p) {
        return visitSee.apply(node, p);
    }

    @Override
    public R visitSerial(SerialTree node, P p) {
        return visitSerial.apply(node, p);
    }

    @Override
    public R visitSerialData(SerialDataTree node, P p) {
        return visitSerialData.apply(node, p);
    }

    @Override
    public R visitSerialField(SerialFieldTree node, P p) {
        return visitSerialField.apply(node, p);
    }

    @Override
    public R visitSince(SinceTree node, P p) {
        return visitSince.apply(node, p);
    }

    @Override
    public R visitStartElement(StartElementTree node, P p) {
        return visitStartElement.apply(node, p);
    }

    @Override
    public R visitText(TextTree node, P p) {
        return visitText.apply(node, p);
    }

    @Override
    public R visitThrows(ThrowsTree node, P p) {
        return visitThrows.apply(node, p);
    }

    @Override
    public R visitUnknownBlockTag(UnknownBlockTagTree node, P p) {
        return visitUnknownBlockTag.apply(node, p);
    }

    @Override
    public R visitUnknownInlineTag(UnknownInlineTagTree node, P p) {
        return visitUnknownInlineTag.apply(node, p);
    }

    @Override
    public R visitValue(ValueTree node, P p) {
        return visitValue.apply(node, p);
    }

    @Override
    public R visitVersion(VersionTree node, P p) {
        return visitVersion.apply(node, p);
    }

    @Override
    public R visitOther(DocTree node, P p) {
        return visitOther.apply(node, p);
    }

    public void setDefaultResultSupplier(Supplier<R> defaultResultSupplier) {
        this.defaultResultSupplier = defaultResultSupplier;
    }

    public void setVisitAttribute(BiFunction<AttributeTree, P, R> visitAttribute) {
        this.visitAttribute = visitAttribute;
    }

    public void setVisitAuthor(BiFunction<AuthorTree, P, R> visitAuthor) {
        this.visitAuthor = visitAuthor;
    }

    public void setVisitComment(BiFunction<CommentTree, P, R> visitComment) {
        this.visitComment = visitComment;
    }

    public void setVisitDeprecated(BiFunction<DeprecatedTree, P, R> visitDeprecated) {
        this.visitDeprecated = visitDeprecated;
    }

    public void setVisitDocComment(BiFunction<DocCommentTree, P, R> visitDocComment) {
        this.visitDocComment = visitDocComment;
    }

    public void setVisitDocRoot(BiFunction<DocRootTree, P, R> visitDocRoot) {
        this.visitDocRoot = visitDocRoot;
    }

    public void setVisitEndElement(BiFunction<EndElementTree, P, R> visitEndElement) {
        this.visitEndElement = visitEndElement;
    }

    public void setVisitEntity(BiFunction<EntityTree, P, R> visitEntity) {
        this.visitEntity = visitEntity;
    }

    public void setVisitErroneous(BiFunction<ErroneousTree, P, R> visitErroneous) {
        this.visitErroneous = visitErroneous;
    }

    public void setVisitIdentifier(BiFunction<IdentifierTree, P, R> visitIdentifier) {
        this.visitIdentifier = visitIdentifier;
    }

    public void setVisitInheritDoc(BiFunction<InheritDocTree, P, R> visitInheritDoc) {
        this.visitInheritDoc = visitInheritDoc;
    }

    public void setVisitLink(BiFunction<LinkTree, P, R> visitLink) {
        this.visitLink = visitLink;
    }

    public void setVisitLiteral(BiFunction<LiteralTree, P, R> visitLiteral) {
        this.visitLiteral = visitLiteral;
    }

    public void setVisitParam(BiFunction<ParamTree, P, R> visitParam) {
        this.visitParam = visitParam;
    }

    public void setVisitReference(BiFunction<ReferenceTree, P, R> visitReference) {
        this.visitReference = visitReference;
    }

    public void setVisitReturn(BiFunction<ReturnTree, P, R> visitReturn) {
        this.visitReturn = visitReturn;
    }

    public void setVisitSee(BiFunction<SeeTree, P, R> visitSee) {
        this.visitSee = visitSee;
    }

    public void setVisitSerial(BiFunction<SerialTree, P, R> visitSerial) {
        this.visitSerial = visitSerial;
    }

    public void setVisitSerialData(BiFunction<SerialDataTree, P, R> visitSerialData) {
        this.visitSerialData = visitSerialData;
    }

    public void setVisitSerialField(BiFunction<SerialFieldTree, P, R> visitSerialField) {
        this.visitSerialField = visitSerialField;
    }

    public void setVisitSince(BiFunction<SinceTree, P, R> visitSince) {
        this.visitSince = visitSince;
    }

    public void setVisitStartElement(BiFunction<StartElementTree, P, R> visitStartElement) {
        this.visitStartElement = visitStartElement;
    }

    public void setVisitText(BiFunction<TextTree, P, R> visitText) {
        this.visitText = visitText;
    }

    public void setVisitThrows(BiFunction<ThrowsTree, P, R> visitThrows) {
        this.visitThrows = visitThrows;
    }

    public void setVisitUnknownBlockTag(BiFunction<UnknownBlockTagTree, P, R> visitUnknownBlockTag) {
        this.visitUnknownBlockTag = visitUnknownBlockTag;
    }

    public void setVisitUnknownInlineTag(BiFunction<UnknownInlineTagTree, P, R> visitUnknownInlineTag) {
        this.visitUnknownInlineTag = visitUnknownInlineTag;
    }

    public void setVisitValue(BiFunction<ValueTree, P, R> visitValue) {
        this.visitValue = visitValue;
    }

    public void setVisitVersion(BiFunction<VersionTree, P, R> visitVersion) {
        this.visitVersion = visitVersion;
    }

    public void setVisitOther(BiFunction<DocTree, P, R> visitOther) {
        this.visitOther = visitOther;
    }

}
