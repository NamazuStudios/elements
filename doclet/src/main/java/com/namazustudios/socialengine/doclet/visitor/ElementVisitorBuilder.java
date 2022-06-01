package com.namazustudios.socialengine.doclet.visitor;

import javax.lang.model.element.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ElementVisitorBuilder<R, P> {

    private static
            <ReturnT, FirstT, SecondT>
            BiFunction<FirstT, SecondT, ReturnT> pass(final Supplier<ReturnT> rSupplier) {
        return (element, rType) -> rSupplier.get();
    }

    private final List<Consumer<FunctionalElementVisitor<R,P>>> operations = new ArrayList<>();

    public ElementVisitorBuilder<R, P> withVisit(final BiFunction<Element, P, R> visit) {
        operations.add(v -> v.setVisit(visit));
        return this;
    }

    public ElementVisitorBuilder<R, P> withVisitPackage(final BiFunction<PackageElement, P, R> visitPackage) {
        operations.add(v -> v.setVisitPackage(visitPackage));
        return this;
    }

    public ElementVisitorBuilder<R, P> withVisitType(final BiFunction<TypeElement, P, R> visitType) {
        operations.add(v -> v.setVisitType(visitType));
        return this;
    }

    public ElementVisitorBuilder<R, P> withVisitVariable(final BiFunction<VariableElement, P, R> visitVariable) {
        operations.add(v -> v.setVisitVariable(visitVariable));
        return this;
    }

    public ElementVisitorBuilder<R, P> withVisitExecutable(final BiFunction<ExecutableElement, P, R> visitExecutable) {
        operations.add(v -> v.setVisitExecutable(visitExecutable));
        return this;
    }

    public ElementVisitorBuilder<R, P> withVisitTypeParameter(final BiFunction<TypeParameterElement, P, R> visitTypeParameter) {
        operations.add(v -> v.setVisitTypeParameter(visitTypeParameter));
        return this;
    }

    public ElementVisitorBuilder<R, P> withVisitUnknown(final BiFunction<Element, P, R> visitUnknown) {
        operations.add(v -> v.setVisitUnknown(visitUnknown));
        return this;
    }

    public ElementVisitorBuilder<R, P> withVisitModule(final BiFunction<ModuleElement, P, R> visitModule) {
        operations.add(v -> v.setVisitModule(visitModule));
        return this;
    }

    public ElementVisitor<R, P> build() {
        final var visitor = new FunctionalElementVisitor<R, P>();
        operations.forEach(op -> op.accept(visitor));
        return visitor;
    }

}
