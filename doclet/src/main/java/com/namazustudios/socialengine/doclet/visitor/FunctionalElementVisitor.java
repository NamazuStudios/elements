package com.namazustudios.socialengine.doclet.visitor;

import javax.lang.model.element.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FunctionalElementVisitor<R, P> implements ElementVisitor<R, P> {

    private static <ReturnT, FirstT, SecondT> BiFunction<ReturnT, FirstT, SecondT> fail() {
        return (r, p) -> {
            throw new UnsupportedOperationException();
        };
    }

    private Function<Element, R> visit = e -> {
        throw new UnsupportedOperationException();
    };

    private BiFunction<PackageElement, P, R> visitPackage = fail();

    private BiFunction<TypeElement, P, R> visitType = fail();

    private BiFunction<VariableElement, P, R> visitVariable = fail();

    private BiFunction<ExecutableElement, P, R> visitExecutable = fail();

    private BiFunction<TypeParameterElement, P, R> visitTypeParameter = fail();

    private BiFunction<Element, P, R> visitUnknown = fail();

    private BiFunction<ModuleElement, P, R> visitModule = fail();

    @Override
    public R visit(Element e, P p) {
        return visit.apply(e);
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
        return visit.apply(e);
    }

    @Override
    public R visitModule(ModuleElement e, P p) {
        return visitModule.apply(e, p);
    }

    public void setVisit(Function<Element, R> visit) {
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
