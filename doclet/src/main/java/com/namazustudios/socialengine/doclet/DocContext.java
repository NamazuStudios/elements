package com.namazustudios.socialengine.doclet;

import com.sun.source.doctree.DocTree;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.Locale;
import java.util.Set;

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
