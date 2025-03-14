package dev.getelements.elements.sdk.test;

/**
 * Enumerates bundles, represented as directories of jar files, used in constructing test elements.
 */
public enum TestElementBundle {

    JAKARTA_RS_DEPENDENCIES("sdk-test-element-ws-3.0.0-SNAPSHOT");

    private final String directoryName;

    TestElementBundle(final String directoryName) {
        this.directoryName = directoryName;
    }

    public String getDirectoryName() {
        return directoryName;
    }

}
