package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocContext;
import com.namazustudios.socialengine.doclet.DocProcessor;
import com.namazustudios.socialengine.doclet.DocRoot;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Collections.emptySet;

public class LDocStubDoclet implements Doclet {

    private static final Logger logger = LoggerFactory.getLogger(LDocStubDoclet.class);

    private Locale locale;

    private Reporter reporter;

    @Override
    public void init(final Locale locale, final Reporter reporter) {
        this.locale = locale;
        this.reporter = reporter;
    }

    @Override
    public String getName() {
        return "luadoc";
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return emptySet();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_11;
    }

    @Override
    public boolean run(final DocletEnvironment environment) {

        final var cxt = new DocContext() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public Reporter getReporter() {
                return reporter;
            }

            @Override
            public DocletEnvironment getEnvironment() {
                return environment;
            }
        };

        for (var type : cxt.getIncludedElements()) {
            for (var processor : DocProcessor.get(cxt, type)) {
                final var stubs = processor.process();
                write(stubs);
            }
        }

        return true;

    }

    private void write(final List<? extends DocRoot> stubs) {
        // TODO: Dump stubs out to disk later
        reporter.print(Diagnostic.Kind.NOTE, "Processed stub: " + stubs);
    }

}
