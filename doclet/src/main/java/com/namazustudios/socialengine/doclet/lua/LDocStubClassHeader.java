package com.namazustudios.socialengine.doclet.lua;

import java.util.ArrayList;
import java.util.List;

public class LDocStubClassHeader {

    private String summary;

    private String description;

    private final String name;

    private final List<String> authors = new ArrayList<>();

    public LDocStubClassHeader(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addAuthor(final String author) {
        authors.add(author);
    }

}
