package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocAnnotations;
import com.namazustudios.socialengine.doclet.DocContext;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

import static com.namazustudios.socialengine.doclet.DocAnnotations.*;

/**
 * Used to process javadoc and annotations and generate instances of {@link LDocStub}.
 *
 * @param <StubT>
 */
public interface LDocProcessor<StubT extends LDocStub> {

    /**
     * Generates zero or more {@link LDocStub} instances.
     *
     * @return the {@link List<StubT>} instances.
     */
    List<StubT> process();

    /**
     * Gets one or more {@link LDocProcessor<?>} instances given the context and {@link TypeElement}
     *
     * @param cxt the context
     * @param typeElement the {@link TypeElement}
     * @return zero or more {@link LDocProcessor<?>} instances
     */
    static List<LDocProcessor<?>> get(final DocContext cxt, final TypeElement typeElement) {

        final List<LDocProcessor<?>> processors = new ArrayList<>();

        final var expose = getExposed(typeElement);
        final var exposeEnum = getExposedEnum(typeElement);
        final var intrinsic = getIntrinsic(typeElement);

        if (expose != null)
            processors.add(new LDocStubProcessorExpose(cxt, typeElement, expose));

        if (exposeEnum != null)
            processors.add(new LDocStubProcessorExpose(cxt, typeElement, exposeEnum));

        if (intrinsic != null)
            processors.add(new LDocStubProcessorIntrinsic(cxt, intrinsic, typeElement));

        if (isStandard(typeElement))
            processors.add(new LDocStubProcessorStandard(cxt, typeElement));

        for (var enclosed : typeElement.getEnclosedElements()) {
            switch (enclosed.getKind()) {
                case ENUM:
                case CLASS:
                case INTERFACE:
                    final var subProcessors = get(cxt, (TypeElement) enclosed);
                    processors.addAll(subProcessors);
                    break;
            }
        }

        return processors;

    }



}
