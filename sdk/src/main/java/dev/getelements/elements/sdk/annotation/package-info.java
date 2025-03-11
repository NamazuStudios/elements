/**
 * The SDK Annotations in this package govern the visibility and services defined within an
 * {@link dev.getelements.elements.sdk.Element}. Where an annotation specifies the {@link java.lang.annotation.Target}
 * as {@link java.lang.annotation.ElementType#PACKAGE}, it must appear on the "package-info" class, or else the loader
 * will ignore the pacakge-level annotations. This is mostly motivated by performance and convenience of the loader
 * code.
 */
@ElementPublic
package dev.getelements.elements.sdk.annotation;
