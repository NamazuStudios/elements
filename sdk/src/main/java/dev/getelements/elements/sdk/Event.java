package dev.getelements.elements.sdk;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an event to be handled by {@link Element} instances.
 */
public interface Event {

    /**
     * All event names with the following prefix are considered system events. Applications should not use events
     * prefixed with this string.
     */
    String SYSTEM_EVENT_PREFIX = "dev.getelements";

    /**
     * True if the event is a system event.
     *
     * @return true if system event, false otherwise.
     */
    default boolean isSystemEvent() {
        return getEventName().startsWith(SYSTEM_EVENT_PREFIX);
    }

    /**
     * The {@link Event} name. The names of events are application specific. However, they should follow a globally
     * unique naming pattern. Additionally, {@link #SYSTEM_EVENT_PREFIX} prefixed events are reserved for the SDK and
     * should not be used.
     *
     * @return the name of hte event.
     */
    String getEventName();

    /**
     * Get the arguments of the {@link Event}. When using argument-based matching, each argument must be non-null and
     * must match based on exact class type. The dispatch mechanism does not honor polymorphism.
     *
     * @return the arguments
     */
    List<Object> getEventArguments();

    /**
     * Gets the argument at the specified index. The index must be within the bounds of the event arguments list.
     *
     * @param index the index of the argument to retrieve
     * @return the argument at the specified index
     * @param <T> the type of the argument to retrieve
     */
    default <T> T getEventArgument(final int index) {
        return (T) getEventArguments().get(index);
    }

    /**
     * Gets the argument at the specified index. The index must be within the bounds of the event arguments list.
     * Similar to {@link #getEventArgument(int)}, thie enforces more predictable exception behavior  by requiring the
     * type to be explicitly named in the method call.
     *
     * @param index the index of the argument to retrieve
     * @return the argument at the specified index
     * @param <T> the type of the argument to retrieve
     */
    default <T> T getEventArgument(final int index, Class<T> type) {
        return type.cast(getEventArguments().get(index));
    }

    /**
     * Returns a new {@link Builder} for {@link Event} instances.
     *
     * @return the {@link Builder}
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Utility class to build {@link Event} instances.
     */
    class Builder {

        private String name;

        private final List<Object> arguments = new ArrayList<>();

        /**
         * Specifies the {@link Event} name.
         *
         * @param name the name of the event
         * @return this instance
         */
        public Builder named(String name) {
            this.name = name;
            return this;
        }

        /**
         * Appends an argument to the {@link Event}.
         *
         * @param argument the argument to append
         * @return this instance
         */
        public Builder argument(final Object argument) {

            if (argument == null) {
                throw new IllegalArgumentException("Argument cannot be null");
            }

            arguments.add(argument);
            return this;

        }

        /**
         * Builds the {@link Event}.
         *
         * @return the {@link Event}
         */
        public Event build() {

            if (name == null) {
                throw new IllegalArgumentException("Event is not named.");
            }

            return new Event() {
                @Override
                public String getEventName() {
                    return name;
                }

                @Override
                public List<Object> getEventArguments() {
                    return List.copyOf(arguments);
                }

            };
        }

    }

}
