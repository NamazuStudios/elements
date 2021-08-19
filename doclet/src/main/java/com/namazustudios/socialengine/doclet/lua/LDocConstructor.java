package com.namazustudios.socialengine.doclet.lua;

import com.namazustudios.socialengine.doclet.DocRootWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Documents a constructor.
 */
public class LDocConstructor {

    private final String name;

    private final String variant;

    private String summary = "";

    private String description = "";

    private final List<LDocParameter> parameters = new ArrayList<>();

    public LDocConstructor(final String variant) {
        this(variant, "new");
    }

    public LDocConstructor(final String variant, final String name) {
        this.variant = variant;
        this.name = name;
    }

    /**
     * Gets the constructor's name.
     *
     * @return the constructor's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the variant.
     *
     * @return the variant
     */
    public String getVariant() {
        return variant;
    }

    /**
     * Gets the summary.
     *
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the summary.
     * @param summary
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Gets the description, if available.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public List<LDocParameter> getParameters() {
        return parameters;
    }

    public LDocParameter addParameter(final String name) {
        final var parameter = new LDocParameter(name);
        parameter.prependDescription(variant);
        getParameters().add(parameter);
        return parameter;
    }

    public LDocParameter addParameter(final String name,
                                      final String typeDescription,
                                      final String comment) {
        final var param = addParameter(name);
        param.setType(typeDescription);
        param.appendDescription(comment);
        return param;
    }

    public void writeSingleConstructor(final DocRootWriter writer) {
        writer.printlnf("--- %s", getSummary());
        writer.println("--");

    }

    public void writeConstructorVariant(final DocRootWriter writer) {

    }

    public void writeConstructorParameters(final DocRootWriter writer) {

    }

}
