package dev.getelements.elements.sdk.model.security;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a PEM label per RFC-7468
 */
public enum Rfc7468Label {

    /**
     * See RFC-5280
     */
    CERTIFICATE,

    /**
     * See RFC-5280
     */
    X509_CRL,

    /**
     * See RFC-2986
     */
    CERTIFICATE_REQUEST,

    /**
     * See RFC-2315
     */
    PKCS7,

    /**
     * See RFC-5652
     */
    CMS,

    /**
     * See RFC-5208 and RFC-5958
     */
    PRIVATE_KEY,

    /**
     * See RFC-5958
     */
    ENCRYPTED_PRIVATE_KEY,

    /**
     * RFC-5755
     */
    ATTRIBUTE_CERTIFICATE,

    /**
     * RFC-5280
     */
    PUBLIC_KEY;

    private final String label = toString().replace('_', ' ');

    /**
     * Gets the literal label value.
     *
     * @return the literal label value
     */
    public String getLabel() {
        return label;
    }

    /**
     * Finds the enum value from the label.
     *
     * @param label the label
     * @return the {@link Optional} containing the value, or empty it not found
     */
    public static Optional<Rfc7468Label> findForLabel(final String label) {
        return Stream.of(values())
                .filter(e -> e.label.equals(label))
                .findFirst();
    }

}
