package com.namazustudios.socialengine.rest.inventory;

import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.*;

@Path("inventory/distinct")
@Api(value = "Inventory",
        description =
                "Manages inventory allowing for multiple stacks of the same item.  Each item stack is placed in the " +
                "priority slot specified in the request. This is used in scenarios where multiple stacks of the same" +
                        "item are required. It is considered a superset of the simple inventory APIs.",
        authorizations = {
                @Authorization(AUTH_BEARER),
                @Authorization(SESSION_SECRET),
                @Authorization(SOCIALENGINE_SESSION_SECRET)
        }
)
@Produces(MediaType.APPLICATION_JSON)
public class DistinctInventoryItemResource {
}
