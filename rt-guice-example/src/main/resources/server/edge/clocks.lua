
-- Used by the scrpt to handle some boilerplate

namazu_internal = require "namazu_internal"
namazu_internal.require "clock"

-- A script global which contains the clocks we want to support in our clock example.  This is pre-loaded with
-- some basic data about clocks aroudnt he world.  This will make a virtual clock and subscribe the client to it
-- and will update the current time of that clock based on the time zone.

-- Let's inject some globals from the IoC container.  This way we aren't asking the server
-- repeatedly for an object if it's already loaded.  This has the added benefit of throwing
-- injection errors when the script is loaded.

-- The simply injects the internal_server instance from Java
internalServer = namazu_rt.ioc:inject("com.namazustudios.socialengine.rt.internal.InternalServer")

-- This gets a Provider<?> which can be used to obtain the instance using the get.  Remember that since
-- the container configures internal resources without scope (typically) then each call to get() involves
-- creation of another Java object.  This should be used only as much as needed.

-- A global table of the clocks we know about

clocks = {
    {
        name = "Big Ben",
        location = "London, England",
        timeZone = "Europe/London",
        path = "big_ben"
    },
    {
        name = "Eastern Columbia Building",
        location = "Los Angeles, California",
        timeZone = "America/Los_Angeles"
    }
}


-- A function to initialize the clock as a reference-counted internal resource.  When calling this funciton,
-- an InternalResource is created with the given clock id, name, and metadata.  This uses the internal server's
-- atomic API to ensure that the resource is instantiated only once.

function get_clock(name, clockTable)
    -- the new clock will live at /clocks/<clock_name>
    path = { "clocks", name }
    return namazu_internal.initialize_if_needed(path, "clock.lua", clockTable)
end

-- The handler for the "list_clocks" method.  This includes the ability to list the clocks
-- to which we want to subscribe.  This is a simple request which actually ignores the payload
-- so it is safe to send a null value.

function namazu_rt.request.list_clocks(session, header, payload)

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
-- from Java and just increment that resource's reference count.  If the clock can't
-- be found then this just returns a not found error.

function namazu_rt.request.subscribe(session, header, payload)

    print("Handling request for path " .. header:getPath() .. " method " .. header:getMethod())

    -- We get the name of the clock from the payload.  REturn an error if the client
    -- forgot to specify

    name = payload.name

    if name == nil then
        return namazu_rt.response_code.BAD_REQUEST_FATAL, {
            message = "clock not specified"
        }
    end

    -- Gets the metadata, making sure to return an error

    metadata = clocks[name]

    -- If we don't have the clock's init params listed, we then will skip
    -- relay that condition to the client.

    if metadata == nil then
        return namazu_rt.response_code.OTHER_NOT_FOUND, {
            message = name .. " not found "
        }
    end

    -- We are creating a reference-counted clock at the path with the name of the clock
    -- if this is the first session connecting, then we will create the resource.  It is
    -- bound to the session so the last client to disconnect will actually remove it
    -- from the server.

    path = "/clocks/" .. path
    namazu_internal.initialize_to_session_if_needed(session, "clock.lua", "/clocks", metadata)

    -- We wil subscribe the client to the "tick tock" and the "ding dong" event.  We specify any
    -- type to indicate  that any object will be serialized to the client.  After this call the
    -- resource will start sending the event down to the client where it can be observed client side

    session:subscribeToInternalEvent():named("tick tock"):ofAnyType();
    session:subscribeToInternalEvent():named("ding dong"):ofAnyType();

    -- Lastly, we need to provide some acknowledgement that the request was successful.  This
    -- is kept as compact as possible.  The client only cares about the operation was successful
    return namazu_rt.response_code.OK, {}

end
