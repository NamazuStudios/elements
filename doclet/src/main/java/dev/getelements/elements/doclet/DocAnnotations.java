package dev.getelements.elements.doclet;

import dev.getelements.elements.doclet.lua.LDocStubProcessorStandard;
import dev.getelements.elements.rt.annotation.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Used to document annotations in various types when processing documentation.
 */
@Private
public class DocAnnotations {

    private DocAnnotations() {}

    /**
     * Tests if the specified type is flagged as {@link Public}
     *
     * @param element the element to test
     * @return true if private, false otherwise
     */
    public static boolean isPublicDoc(final Element element) {

        var e = element;

        do {
            final var priv = e.getAnnotation(Public.class);
            if (priv != null) return true;
        } while((e = e.getEnclosingElement()) != null && !isTopLevel(e));

        return false;

    }

    /**
     * Tests if the specified type is flagged as {@link Private}
     *
     * @param element the element to test
     * @return true if private, false otherwise
     */
    public static boolean isPrivateDoc(final Element element) {

        var e = element;

        do {
            final var priv = e.getAnnotation(Private.class);
            if (priv != null) return true;
        } while((e = e.getEnclosingElement()) != null && !isTopLevel(e));

        return false;

    }

    private static boolean isTopLevel(final Element element) {
        switch (element.getKind()) {
            case MODULE:
            case PACKAGE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Tests if the supplied {@link TypeElement} should be handled by the {@link LDocStubProcessorStandard},
     *
     * @param typeElement the {@link TypeElement} to test
     * @return true if the standard processor applies, false otherwise.
     */
    public static boolean isStandard(final TypeElement typeElement) {
        switch (typeElement.getKind()) {
            case ENUM:
            case CLASS:
            case INTERFACE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Tests if the type is exposed via the {@link Expose} annotation.
     *
     * @param typeElement the type element
     * @return an instance of {@link Expose}, or null if the module isn't {@link Expose}d
     */
    public static Expose getExposed(final TypeElement typeElement) {
        switch (typeElement.getKind()) {
            case CLASS:
            case INTERFACE:
                return isPrivateDoc(typeElement) ? null : typeElement.getAnnotation(Expose.class);
            default:
                return null;
        }
    }

    /**
     * Tests if the type is exposed via the {@link ExposeEnum} annotation.
     *
     * @param typeElement the type element
     * @return an instance of {@link ExposeEnum}, or null if the module isn't {@link ExposeEnum}
     */
    public static ExposeEnum getExposedEnum(final TypeElement typeElement) {
        switch (typeElement.getKind()) {
            case ENUM:
                return isPrivateDoc(typeElement) ? null : typeElement.getAnnotation(ExposeEnum.class);
            default:
                return null;
        }
    }

    /**
     * Gets an instance of {@link Intrinsic} to be processed into intrinsic doc.
     *
     * @param element the {@link TypeElement}
     * @return the instance of {@link Intrinsic} or null if not
     */
    public static Intrinsic getIntrinsic(final Element element) {
        return isPrivateDoc(element) ? null : element.getAnnotation(Intrinsic.class);
    }

}
