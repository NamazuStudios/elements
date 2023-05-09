package dev.getelements.elements.doclet.metadata;

import dev.getelements.elements.rt.annotation.Intrinsic;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.getelements.elements.doclet.metadata.CodeStyleMetadata.JAVA_11;
import static java.util.Arrays.stream;

/**
 * Module definition medata. Typically maps to the values defined in {@link ModuleDefinition}
 */
public interface ModuleDefinitionMetadata {

    /**
     * Gets the name of the module.
     *
     * @return the module name
     */
    String getName();

    /**
     * Gets the input {@link CodeStyleMetadata}. This is the source of the original code style.
     *
     * @return the input code style.
     */
    default CodeStyleMetadata getInputCodeStyle() {
        return JAVA_11;
    }

    /**
     * Gets the output {@link CodeStyleMetadata}. This is the style of the generated documentation.
     *
     * @return the code style
     */
    CodeStyleMetadata getOutputCodeStyle();

    /**
     * Gets the {@link DeprecationMetadata} for this module.
     *
     * @return the {@link DeprecationMetadata}
     */
    DeprecationMetadata getDeprecationMetadata();

    /**
     * Creates a {@link ModuleDefinitionMetadata} from the supplied {@link ModuleDefinition}.
     *
     * @param moduleDefinition the {@link ModuleDefinition}
     * @return the resulting {@link ModuleDefinitionMetadata}
     */
    static ModuleDefinitionMetadata fromAnnotation(final ModuleDefinition moduleDefinition) {

        final var codeStyle = CodeStyleMetadata.from(moduleDefinition.style());
        final var deprecation = DeprecationMetadata.from(moduleDefinition.deprecated());

        return new ModuleDefinitionMetadata() {

            @Override
            public String getName() {
                return moduleDefinition.value();
            }

            @Override
            public CodeStyleMetadata getInputCodeStyle() {
                return JAVA_11;
            }

            @Override
            public CodeStyleMetadata getOutputCodeStyle() {
                return codeStyle;
            }

            @Override
            public DeprecationMetadata getDeprecationMetadata() {
                return deprecation;
            }

        };

    }

    /**
     * Creates a stream of {@link ModuleDefinitionMetadata} from the supplied {@link Intrinsic}, which may define
     * several modules.
     *
     * @param intrinsic the {@link Intrinsic} to use
     * @return a {@link Stream<ModuleDefinitionMetadata>} for each defined module in the {@link Intrinsic}.
     */
    static Stream<ModuleDefinitionMetadata> streamFrom(final Intrinsic intrinsic) {
        final var value = intrinsic.value();
        return stream(value, 0, intrinsic.value().length)
                .map(def -> new ModuleDefinitionMetadata() {

                    private final CodeStyleMetadata style = CodeStyleMetadata.from(def.style());

                    @Override
                    public String getName() {
                        return def.value();
                    }

                    @Override
                    public CodeStyleMetadata getInputCodeStyle() {
                        return style;
                    }

                    @Override
                    public CodeStyleMetadata getOutputCodeStyle() {
                        return style;
                    }

                    @Override
                    public DeprecationMetadata getDeprecationMetadata() {
                        return DeprecationMetadata.from(def.deprecated());
                    }

                });
        }

    static ModuleDefinitionMetadata fromJavaClassName(final String className, final Deprecated deprecated) {
        return new ModuleDefinitionMetadata() {

            @Override
            public String getName() {
                return className;
            }

            @Override
            public CodeStyleMetadata getInputCodeStyle() {
                return JAVA_11;
            }

            @Override
            public CodeStyleMetadata getOutputCodeStyle() {
                return JAVA_11;
            }

            @Override
            public DeprecationMetadata getDeprecationMetadata() {
                return DeprecationMetadata.from(deprecated);
            }

        };
    }

}
