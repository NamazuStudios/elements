package com.namazustudios.socialengine.docserve;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.SourceVersion;
import javax.tools.ToolProvider;
import java.util.Locale;
import java.util.Set;

import static java.util.Collections.emptySet;

public class LuaDoclet implements Doclet {

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
        final var trees = environment.getDocTrees();
        return false;
    }

    public static void main(String[] args) {
        ToolProvider.getSystemDocumentationTool().run(
            System.in, System.out, System.err,
            "-doclet", LuaDoclet.class.getName()
        );
    }

}
