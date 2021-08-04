package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.rt.annotation.Private;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.Locale;
import java.util.Set;

/**
 * A context for processing documentation tags.
 */
@Private
public interface DocContext {

    /**
     * The requested locale.
     *
     * @return requested locale.
     */
    Locale getLocale();

    /**
     * The error/log reporter used.
     *
     * @return the {@link Reporter}
     */
    Reporter getReporter();

    /**
     * Returns the {@link DocletEnvironment} used in conjunction with this instance.
     *
     * @return the {@link DocletEnvironment}
     */
    DocletEnvironment getEnvironment();

    /**
     * Gets the {@link DocTrees} with the {@link DocletEnvironment} contained in this {@link DocContext}.
     *
     * @return the {@link DocTrees}
     */
    default DocTrees getDocTrees() {
        return getEnvironment().getDocTrees();
    }

    /**
     * Gets all included {@link TypeElement}s. Note: this does nothing to filter elements which may be flagged
     * {@link Private}
     *
     * @return the {@link Set<TypeElement>} for types to consider for processing.
     */
    default Set<TypeElement> getIncludedElements() {
        return ElementFilter.typesIn(getEnvironment().getIncludedElements());
    }

}
