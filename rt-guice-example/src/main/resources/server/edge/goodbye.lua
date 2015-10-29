
-- Tells the container that the file needs to be  bootstrapped at
-- tthe given location.  It is important to note that this is only
-- necessary for edge resources as edge resources are automatically
-- installed by the container.  Internal resources, on the other hand
-- are installed manually by the client scripts.

namazu_rt.bootstrap_path = "/goodbye"

namazu_rt.request.goodbye = function(client, request)

    -- Disconnects the client and returns a simple message indicating so.

    client:disconnect();

    return 0, {
        message = "Thanks for playing!",
        details = "You will be disconnected now.  Goodbye!"
    }

end
