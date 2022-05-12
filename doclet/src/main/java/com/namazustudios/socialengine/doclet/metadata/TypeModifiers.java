package com.namazustudios.socialengine.doclet.metadata;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Tests various types for modifiers.
 */
public class TypeModifiers {

    /**
     * Checks if the specified {@link VariableElement} is a constant. A constant is defined as a static final field
     * that is enclosed by a class, enum, interface, or annotation type.
     *
     * @param element the element
     *
     * @return true if the {@link VariableElement} is a constant, fasle otherwise.
     */
    public static boolean isConstant(final Element element) {

        if (!element.getKind().isField()) return false;

        switch (element.getEnclosingElement().getKind()) {
            case ENUM:
            case CLASS:
            case INTERFACE:
            case ANNOTATION_TYPE: {
                final var modifiers = element.getModifiers();
                return modifiers.contains(Modifier.STATIC) && modifiers.contains(Modifier.FINAL);
            }
            default:
                return false;
        }

    }

    /**
     * Returns true if the supplied {@link Element} is public.
     *
     * @param element the public element
     *
     * @return true if public, false otherwise
     */
    public static boolean isPublicModifier(final Element element) {
        return element.getModifiers().contains(PUBLIC);
    }

    /**
     * Returns true if the supplied {@link Element} is public.
     *
     * @param element the public element
     *
     * @return true if public, false otherwise
     */
    public static boolean isProtectedModifier(final Element element) {
        return element.getModifiers().contains(PUBLIC);
    }

}
