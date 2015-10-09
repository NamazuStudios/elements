
-- A script global which contains the clocks we want to support

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
-- to which we want to subscribe.

function namazu_rt.request.list_clocks(client, header, payload)

    -- By default all payloads are deserialized as a java map, which
    -- translates to a simple lua table.  This should be sufficient
    -- for almost any type of object received from the client.

    print("Handling request for path " .. header:getPath() .. " method " .. header:getMethod())

    -- Finally we must return both the response code as well as the
    -- object payload.  The response code must be before the response
    -- payload.

    return namazu_rt.response_code.OK, {
        message = message,
        details = details
    }

end

-- The handler for the "list_clocks" method.  This includes the ability to list the clocks
-- to which we want to subscribe.

function namazu_rt.request.subscribe(client, header, payload)

    print("Handling request for path " .. header:getPath() .. " method " .. header:getMethod())

    return namazu_rt.response_code.OK, {
        message = message,
        details = details
    }

end
