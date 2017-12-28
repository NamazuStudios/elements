package com.namazustudios.socialengine.appnode.jeromq;

import com.namazustudios.socialengine.model.application.Application;

import java.nio.charset.Charset;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.UUID.nameUUIDFromBytes;

/**
 * Used to generate various URLs for internal routing of messages.
 */
public class Routing {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Gets a route {@link UUID} for the supplied {@link Application}.  The expected id corresponds to the value
     * returned by {@link Application#getId()}.  A route ID is defined as a Type 3 UUID derived from the UTF-8 encoding
     * of the supplied {@link String}.  This is used to abbreviate long database IDs over the network and guarantee
     * a fixed-length identifier for the application.
     *
     * @param applicationId the application's ID
     * @return a {@link UUID} based on the application id
     */
    public UUID getRouteId(final String applicationId) {
        return nameUUIDFromBytes(applicationId.getBytes(UTF_8));
    }

    /**
     * Derives a URL based address for the internal routing of requests for a particular {@link Application} with the
     * associated route {@link UUID}. This is defined as the inproc://route-[routeId].
     *
     * Returns the {@link String} representing the internal route address.
     **/
    public String getAddressForRouteId(final UUID routeId) {
        return format("inproc://route-%s", routeId);
    }

    /**
     * Derives a URL based address for the internal routing of requests for a particular {@link Application}.  This is
     * defined as the inproc://route-[routeId] (where routeId is the value of {@link #getRouteId(String)}.
     *
     * Returns the {@link String} representing the internal route address.
     **/
    public String getAddressForApplicationId(final String applicationId) {
        final UUID routeId = getRouteId(applicationId);
        return getAddressForRouteId(routeId);
    }

}
