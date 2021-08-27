package com.namazustudios.socialengine.doclet.metadata;

import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;

/**
 * Defines deprecation information.
 */
public interface DeprecationMetadata {

    /**
     * Constant to {@link DeprecationMetadata}.
     */
    DeprecationMetadata NOT_DEPRECATED = new DeprecationMetadata() {
        @Override
        public boolean isDeprecated() {
            return false;
        }

        @Override
        public String getDeprecationMessage() {
            return "";
        }
    };

    static DeprecationMetadata from(final Deprecated deprecated) {
        return deprecated == null ? NOT_DEPRECATED : new DeprecationMetadata() {

            @Override
            public boolean isDeprecated() {
                return true;
            }

            @Override
            public String getDeprecationMessage() {
                return deprecated.toString().substring(1);
            }

        };
    }

    /**
     * Returns true if deprecated.
     *
     * @return true if deprecated.
     */
    boolean isDeprecated();

    /**
     * Returns the deprecation message. If {@link #isDeprecated()} is false this method's result may be ignored.
     *
     * @return the deprecation message
     */
    String getDeprecationMessage();

    /**
     * Creates a {@link DeprecationMetadata} from the supplied {@link Deprecated} annotation.
     *
     * @param deprecated the deprecated annotation
     * @return the {@link DeprecationMetadata}
     */
    static DeprecationMetadata from(final DeprecationDefinition deprecated) {
        return new DeprecationMetadata() {

            @Override
            public boolean isDeprecated() {
                return deprecated.deprecated();
            }

            @Override
            public String getDeprecationMessage() {
                return deprecated.value();
            }

        };
    }

}
