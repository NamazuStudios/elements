package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.rt.annotation.Private;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.Locale;
import java.util.Set;

@Private
public interface DocContext {

    Locale getLocale();

    Reporter getReporter();

    DocletEnvironment getEnvironment();

    default DocTrees getDocTrees() {
        return getEnvironment().getDocTrees();
    }

    default Set<TypeElement> getIncludedElements() {
        return ElementFilter.typesIn(getEnvironment().getIncludedElements());
    }

}
