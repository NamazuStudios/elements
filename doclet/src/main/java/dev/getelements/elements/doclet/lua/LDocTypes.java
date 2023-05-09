package dev.getelements.elements.doclet.lua;

import dev.getelements.elements.doclet.visitor.ElementVisitorBuilder;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposeEnum;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.type.TypeKind.*;

/**
 * Maps Java types to descriptions of Lua types. This supports all generic types and will recurse into for all generic
 * types found in Java types.
 */
public class LDocTypes {

    private static final Map<TypeKind, Function<TypeMirror, String>> TYPE_KINDS;

    static {
        final var map = new EnumMap<TypeKind, Function<TypeMirror, String>>(TypeKind.class);
        map.put(VOID, typeMirror -> null);
        map.put(BOOLEAN, typeMirror -> "bool");
        map.put(BYTE, typeMirror -> "number");
        map.put(SHORT, typeMirror -> "number");
        map.put(INT, typeMirror -> "number");
        map.put(LONG, typeMirror -> "number");
        map.put(CHAR, typeMirror -> "number");
        map.put(FLOAT, typeMirror -> "number");
        map.put(DOUBLE, typeMirror -> "number");
        map.put(TYPEVAR, typeMirror -> "generic");
        map.put(ARRAY, LDocTypes::forArrayType);
        map.put(DECLARED, LDocTypes::forDeclaredType);
        TYPE_KINDS = unmodifiableMap(map);
    }

    /**
     * Returns type description.
     *
     * @param typeMirror the type mirror.
     *
     * @return the type description, or null if there should be no return description
     */
    public static String getTypeDescription(final TypeMirror typeMirror) {
        return TYPE_KINDS
            .getOrDefault(typeMirror.getKind(), tk -> typeMirror.toString())
            .apply(typeMirror);
    }

    private static String forArrayType(final TypeMirror typeMirror) {
        final var arrayType = (ArrayType) typeMirror;
        final var componentType = arrayType.getComponentType();
        return format("{%s}", getTypeDescription(componentType));
    }

    private static String forDeclaredType(final TypeMirror typeMirror) {

        final var declaredType = (DeclaredType) typeMirror;

        final var fqn = declaredType.asElement().accept(new ElementVisitorBuilder<String, Void>()
            .withVisitType((t, unused) -> t.getQualifiedName().toString())
            .build(), null
        );

        try {

            final var cls = Class.forName(fqn);

            if (Map.class.isAssignableFrom(cls)) {
                return forMapType(declaredType);
            } else if (List.class.isAssignableFrom(cls)) {
                return forListType(declaredType);
            } else if (Number.class.isAssignableFrom(cls)) {
                return "number";
            } else if (String.class.isAssignableFrom(cls)) {
                return "string";
            } else {
                return forGenericType(declaredType);
            }

        } catch (ClassNotFoundException e) {
            return forGenericType(declaredType);
        }

    }

    private static String forMapType(final DeclaredType declaredType) {

        final var arguments = declaredType.getTypeArguments();
        if (arguments.size() < 2) return "{}";

        final var keyType = arguments.get(0);
        final var valType = arguments.get(1);

        final var keyTypeDescription = getTypeDescription(keyType);
        final var valTypeDescription = getTypeDescription(valType);

        return format("{[%s]=%s}", keyTypeDescription, valTypeDescription);

    }

    private static String forListType(final DeclaredType declaredType) {

        final var arguments = declaredType.getTypeArguments();
        if (arguments.size() < 1) return "{}";

        final var elementType = arguments.get(0);
        final var elementTypeDescription = getTypeDescription(elementType);
        return format("{%s}", elementTypeDescription);

    }

    private static String forGenericType(final DeclaredType declaredType) {

        final var typeArguments = declaredType.getTypeArguments()
            .stream()
            .map(LDocTypes::getTypeDescription)
            .collect(joining(","));

        final var typeElement = (TypeElement) declaredType.asElement();

        final var exposeAnnotation = typeElement.getAnnotation(Expose.class);
        final var exposeEnumAnnotation = typeElement.getAnnotation(ExposeEnum.class);

        return streamTypeNames(exposeAnnotation, exposeEnumAnnotation)
            .findFirst()
            .orElseGet(() -> typeArguments.isEmpty()
                ? typeElement.getQualifiedName().toString()
                : format("%s(%s)", "", "")
            );

    }

    private static Stream<String> streamTypeNames(final Expose expose, final ExposeEnum exposeEnum) {
        return Stream.concat(
            expose == null ? Stream.empty() : streamTypeNames(expose.value(), expose.modules()),
            exposeEnum == null ? Stream.empty() : streamTypeNames(exposeEnum.value(), exposeEnum.modules())
        );
    }

    private static Stream<String> streamTypeNames(
            final ModuleDefinition [] moduleDefinitions,
            final String[] deprecatedModuleNames) {
        return Stream.concat(
            Arrays.stream(moduleDefinitions).map(ModuleDefinition::value),
            Arrays.stream(deprecatedModuleNames)
        );
    }

}
