package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.event.EventType;

/**
 * Represents a one-way message produced by a Resource, and dispatched to various
 * clients both internal and external to the system.
 *
 * Created by patricktwohig on 8/8/15.
 */
public interface Event {

    /**
     * Gets the event header.
     *
     * @return the event heder
     */
    EventHeader getEventHeader();

    /**
     * Gets the payload for the event.
     *
     * @return the payload
     */
    Object getPayload();

    class Util {

        private Util() {}

        /**
         * Returns the given type's event name, if applicaable.
         *
         * @param object the object itself
         * @return the event name.
         */
        public static String getEventNameFromObject(Object object) {
            return getEventName(object.getClass());
        }

        /**
         * Returns the given type's event name, if applicaable.
         *
         * @param cls
         * @return the event name.
         */
        public static String getEventName(Class<?> cls) {

            final EventType eventType = cls.getAnnotation(EventType.class);

            if (eventType == null) {
                throw new IllegalArgumentException(cls + " is not an event.");
            }

            final String trimmed = eventType.value().trim();
            return trimmed.isEmpty() ? cls.getSimpleName() : trimmed;

        }

    }

}
