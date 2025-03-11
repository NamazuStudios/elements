package dev.getelements.elements.sdk.test;

import java.util.Map;

/**
 * Represents a specific test artifact in the collection of test assets.
 */
public enum TestElementArtifact {

    BASE(
            "sdk-test-element-2.2.0-SNAPSHOT",
            "dev.getelements.elements.sdk.test.element"
    ),
    VARIANT_A(
            "sdk-test-element-a-2.2.0-SNAPSHOT",
            "dev.getelements.elements.sdk.test.element.a"
    ),
    VARIANT_B("sdk-test-element-b-2.2.0-SNAPSHOT",
            "dev.getelements.elements.sdk.test.element.b"
    ),
    JAKARTA_RS("sdk-test-element-rs-2.2.0-SNAPSHOT.jar",
            "dev.getelements.elements.sdk.test.element.rs"
    ),
    JAKARTA_WS("sdk-test-element-ws-2.2.0-SNAPSHOT.jar",
            "dev.getelements.elements.sdk.test.element.ws"
    );

    private final String artifact;

    private final String elementName;

    private final Map<?, ?> attributes = Map.of("dev.getelements.test.variant", toString());

    TestElementArtifact(final String artifact, final String elementName) {
        this.artifact = artifact;
        this.elementName = elementName;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getElementName() {
        return elementName;
    }

    public Map<?, ?> getAttributes() {
        return attributes;
    }

}
