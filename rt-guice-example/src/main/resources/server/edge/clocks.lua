
-- A script global which contains the clocks we want to support in our clock example.  This is pre-loaded with
-- some basic data about clocks aroudnt he world.  This will make a virtual clock and subscribe the client to it
-- and will update the current time of that clock based on the time zone.

-- Let's inject some globals from the IoC container.  This way we aren't asking the server
-- repeatedly for an object if it's already loaded.  This has the added benefit of throwing
-- injection errors when the script is loaded.

-- The simply injects the internal_server instance from Java
internal_server = namazu_rt.ioc:inject("com.namazustudios.socialengine.rt.internal.InternalServer")

-- This gets a Provider<?> which can be used to obtain the instance using the get.  Remember that since
-- the container configures internal resources without scope (typically) then each call to get() involves
-- creation of another Java object.  This should be used only as much as needed.

clocks_script_provider = namazu_rt.ioc:provider("com.namazustudios.socialengine.rt.lua.LuaInternalResource", "clock.lua")

-- A global table of the clocks we know about

clocks = {
    big_ben = {
        name = "Big Ben",
        location = "London, England",
        time_zone = "Europe/London"
    },
    eastern_columbia = {
        name = "Eastern Columbia Building",
        location = "Los Angeles, California",
        time_zone = "America/Los_Angeles"
    }
}


-- The handler for the "list_clocks" method.  This includes the ability to list the clocks
-- to which we want to subscribe.  This is a simple request which actually ignores the payload
-- so it is safe to send a null value.

function namazu_rt.request.list_clocks(client, header, payload)

    print("Handling request for path " .. header:getPath() .. " method " .. header:getMethod())

    message = "Here are the cloks I know about."

    -- Return both the response code as well as the  object payload.
    -- The response code must be before the response payload.

    return namazu_rt.response_code.OK, {
        message = message,
        objects = clocks
    }

end

-- The handler for the "subscribe" method.  This will attempt to create the resource
-- at the path.  If the resource already exists, we can catch the exception thrown
-- from Java and just increment that resource's reference count.

function namazu_rt.request.subscribe(client, header, payload)

    print("Handling request for path " .. header:getPath() .. " method " .. header:getMethod())

    message = "Here are the cloks I know about."

    -- Return both the response code as well as the  object payload.
    -- The response code must be before the response payload.

    return namazu_rt.response_code.OK, {
        message = message,
        objects = clocks
    }

end
