package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.doclet.lua.LDocStubProcessorStandard;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposeEnum;
import com.namazustudios.socialengine.rt.annotation.Intrinsic;
import com.namazustudios.socialengine.rt.annotation.Private;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Used to document annotations in various types when processing documentation.
 */
@Private
public class DocAnnotations {

    private DocAnnotations() {}

    /**
     * Tests if the specified type is flagged as {@link Private}
     *
     * @param element the element to test
     * @return true if private, false otherwise
     */
    public static boolean isPrivate(final Element element) {

        var e = element;

        do {
            final var priv = e.getAnnotation(Private.class);
            if (priv != null) return true;
        } while(!isTopLevel(e) && (e = element.getEnclosingElement()) != null);

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
                return !isPrivate(typeElement);
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
                return isPrivate(typeElement) ? null : typeElement.getAnnotation(Expose.class);
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
                return isPrivate(typeElement) ? null : typeElement.getAnnotation(ExposeEnum.class);
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
        return isPrivate(element) ? null : element.getAnnotation(Intrinsic.class);
    }

}
