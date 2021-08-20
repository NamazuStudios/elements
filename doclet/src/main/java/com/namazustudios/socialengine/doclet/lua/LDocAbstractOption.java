package com.namazustudios.socialengine.doclet.lua;

import jdk.javadoc.doclet.Doclet;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.List.copyOf;
import static jdk.javadoc.doclet.Doclet.Option.Kind.STANDARD;

public abstract class LDocAbstractOption implements Doclet.Option {

    private final int argumentCount;

    private final String description;

    private final String parameters;

    private final List<String> names;

    public LDocAbstractOption(final int argumentCount,
                              final String description,
                              final String parameters,
                              final String ... names) {
        this(argumentCount, description, parameters, asList(names));
    }

    public LDocAbstractOption(final int argumentCount,
                              final String description,
                              final String parameters,
                              final List<String> names) {
        this.argumentCount = argumentCount;
        this.description = description;
        this.parameters = parameters;
        this.names = copyOf(names);
    }

    @Override
    public int getArgumentCount() {
        return argumentCount;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Kind getKind() {
        return STANDARD;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public String getParameters() {
        return parameters;
    }

}
