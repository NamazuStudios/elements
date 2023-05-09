package dev.getelements.elements.doclet.visitor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

public class FunctionalElementVisitor<R, P> implements ElementVisitor<R, P> {

    private Supplier<R> initialResultSupplier = () -> null;

    private BinaryOperator<R> resultReducer = (r1, r2) -> r2;

    private <ElementT extends Element> BiFunction<ElementT, P, R> recurse() {
        return (r, p) -> r.getEnclosedElements()
            .stream()
            .map(e -> e.accept(this, p))
            .reduce(initialResultSupplier.get(), resultReducer);
    }

    private BiFunction<Element, P, R> visit = recurse();

    private BiFunction<PackageElement, P, R> visitPackage = recurse();

    private BiFunction<TypeElement, P, R> visitType = recurse();

    private BiFunction<VariableElement, P, R> visitVariable = recurse();

    private BiFunction<ExecutableElement, P, R> visitExecutable = recurse();

    private BiFunction<TypeParameterElement, P, R> visitTypeParameter = recurse();

    private BiFunction<Element, P, R> visitUnknown = recurse();

    private BiFunction<ModuleElement, P, R> visitModule = recurse();

    @Override
    public R visit(Element e, P p) {
        return visit.apply(e, p);
    }

    @Override
    public R visitPackage(PackageElement e, P p) {
        return visitPackage.apply(e, p);
    }

    @Override
    public R visitType(TypeElement e, P p) {
        return visitType.apply(e, p);
    }

    @Override
    public R visitVariable(VariableElement e, P p) {
        return visitVariable.apply(e, p);
    }

    @Override
    public R visitExecutable(ExecutableElement e, P p) {
        return visitExecutable.apply(e, p);
    }

    @Override
    public R visitTypeParameter(TypeParameterElement e, P p) {
        return visitTypeParameter.apply(e, p);
    }

    @Override
    public R visitUnknown(Element e, P p) {
        return visitUnknown.apply(e, p);
    }

    @Override
    public R visit(Element e) {
        return visit.apply(e, null);
    }

    @Override
    public R visitModule(ModuleElement e, P p) {
        return visitModule.apply(e, p);
    }

    public void setInitialResultSupplier(Supplier<R> initialResultSupplier) {
        this.initialResultSupplier = initialResultSupplier;
    }

    public void setResultReducer(BinaryOperator<R> resultReducer) {
        this.resultReducer = resultReducer;
    }

    public void setVisit(BiFunction<Element, P, R> visit) {
        this.visit = visit;
    }

    public void setVisitPackage(BiFunction<PackageElement, P, R> visitPackage) {
        this.visitPackage = visitPackage;
    }

    public void setVisitType(BiFunction<TypeElement, P, R> visitType) {
        this.visitType = visitType;
    }

    public void setVisitVariable(BiFunction<VariableElement, P, R> visitVariable) {
        this.visitVariable = visitVariable;
    }

    public void setVisitExecutable(BiFunction<ExecutableElement, P, R> visitExecutable) {
        this.visitExecutable = visitExecutable;
    }

    public void setVisitTypeParameter(BiFunction<TypeParameterElement, P, R> visitTypeParameter) {
        this.visitTypeParameter = visitTypeParameter;
    }

    public void setVisitUnknown(BiFunction<Element, P, R> visitUnknown) {
        this.visitUnknown = visitUnknown;
    }

    public void setVisitModule(BiFunction<ModuleElement, P, R> visitModule) {
        this.visitModule = visitModule;
    }

}
