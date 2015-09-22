
-- Tells the container that the file needs to be  bootstrapped at
-- tthe given location.  It is important to note that this is only
-- necessary for edge resources as edge resources are automatically
-- installed by the container.  Internal resources, on the other hand
-- are installed manually by the client scripts.

namazu_rt.bootstrap_path = "/hello"

namazu_rt.request.hello = function(client, request)

    -- By default all payloads are deserialized as a java map, which
    -- translates to a simple lua table.  This should be sufficient
    -- for almost any type of object received from the client.

    name = request.getPayload().name;

    -- Construct the details for the response.  In this case, we send a
    -- simple message that includes the name of the user as well as a
    -- detailed message.  Remember the server is authoratative and it
    -- is up to the client to figure out the response.

    message = string.format("Hello %s.  Nice to meet you.", name)
    details = string.format("You are conneced as %q", client.getId())

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
