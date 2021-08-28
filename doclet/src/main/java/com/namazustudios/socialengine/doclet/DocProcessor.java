package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.doclet.lua.LDocStubProcessorExpose;
import com.namazustudios.socialengine.doclet.lua.LDocStubProcessorIntrinsic;
import com.namazustudios.socialengine.doclet.lua.LDocStubProcessorStandard;
import com.namazustudios.socialengine.rt.annotation.Private;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

import static com.namazustudios.socialengine.doclet.DocAnnotations.*;

/**
 * Used to process javadoc and annotations and generate instances of {@link DocRoot}.
 *
 * @param <StubT>
 */
@Private
public interface DocProcessor<StubT extends DocRoot> {

    /**
     * Generates zero or more {@link DocRoot} instances.
     *
     * @return the {@link List<StubT>} instances.
     */
    List<StubT> process();

    /**
     * Gets one or more {@link DocProcessor <?>} instances given the context and {@link TypeElement}
     *
     * @param cxt the context
     * @param typeElement the {@link TypeElement}
     * @return zero or more {@link DocProcessor <?>} instances
     */
    static List<DocProcessor<?>> get(final DocContext cxt, final TypeElement typeElement) {

        final List<DocProcessor<?>> processors = new ArrayList<>();

        final var expose = getExposed(typeElement);
        final var exposeEnum = getExposedEnum(typeElement);
        final var intrinsic = getIntrinsic(typeElement);

        boolean skipStandardProcessing = false;

        if (expose != null) {
            skipStandardProcessing = true;
            processors.add(new LDocStubProcessorExpose(cxt, typeElement, expose));
        }


        if (exposeEnum != null) {
            skipStandardProcessing = true;
            processors.add(new LDocStubProcessorExpose(cxt, typeElement, exposeEnum));
        }

        if (intrinsic != null) {
            skipStandardProcessing = true;
            processors.add(new LDocStubProcessorIntrinsic(intrinsic));
        }

        if (!skipStandardProcessing && isStandard(typeElement)) {
            processors.add(new LDocStubProcessorStandard(cxt, typeElement));
        }

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
