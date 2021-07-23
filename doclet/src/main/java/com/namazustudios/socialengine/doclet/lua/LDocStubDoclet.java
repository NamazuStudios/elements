package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocContext;
import com.namazustudios.socialengine.rt.annotation.Expose;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import static java.util.Collections.emptySet;
import static javax.tools.Diagnostic.Kind.ERROR;

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
            logger.info("Processing {}", type);
            final var expose = type.getAnnotation(Expose.class);
            if (expose != null) {
                documentExposedType(cxt, expose, type);
            }
        }

        return true;
    }

    private void documentExposedType(final DocContext cxt,
                                     final Expose expose,
                                     final TypeElement type) {
        try (var processor = new LDocStubProcessorExpose(cxt, type, expose)) {
            final var pages = processor.process();
            for (var page : pages) {
                logger.info("Page:" + page);
            }
        } catch (IOException ex) {
            reporter.print(ERROR, "Unable to write page for " + type.getQualifiedName() + ": " + ex.getMessage());
        }
    }

}
