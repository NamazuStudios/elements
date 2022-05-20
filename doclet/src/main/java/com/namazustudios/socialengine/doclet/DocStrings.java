package com.namazustudios.socialengine.doclet;

import com.google.common.base.Strings;

public class DocStrings {

    private DocStrings() {}

    public static String sanitize(final String input) {
        return Strings.nullToEmpty(input).trim().replaceAll("[\\r\\n]", "");
    }

}
