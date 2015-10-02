
-- The handler for the "introduce_yourself" method.  This will accept the client and the request
-- as paramters and generate a response.  The method must return the response immediately
-- in response to the request.

function namazu_rt.request.introduce_yourself(client, header, payload)

    -- By default all payloads are deserialized as a java map, which
    -- translates to a simple lua table.  This should be sufficient
    -- for almost any type of object received from the client.

    print("Handling request for path " .. header:getPath() .. " method " .. header:getMethod())

    name = payload.name

    -- Construct the details for the response.  In this case, we send a
    -- simple message that includes the name of the user as well as a
    -- detailed message.  Remember the server is authoratative and it
    -- is up to the client to figure out the response.

    message = string.format("Hello %s.  Nice to meet you.", name)
    details = string.format("You are conneced as %q", client:getId())

    -- Finally we must return both the response code as well as the
    -- object payload.  The response code must be before the response
    -- payload.

    return 0, {
        message = message,
        details = details
    }

end

-- Container is a table set by the containing appplication.  Basically it
-- provides some automtically managed coroutines.
namazu_rt.coroutine.create(function(deltaTime)

    timer = deltaTime;
    cycles = 1

    while true do

        timer = timer + coroutine.yield()

        if (timer >= 10.0) then
            timer = 0
            cycles = cycles + 1
            namazu_rt.bridge.resource:getScriptLog():info("Hello World {}! ", cycles)
        end

    end

end)
