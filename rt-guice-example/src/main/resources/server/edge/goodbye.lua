
-- This is a simple function that is used to disconnect from the remote server.  The
-- the session is disconnected and then the

namazu_rt.request.goodbye = function(session, request)

    -- Disconnects the client and returns a simple message indicating so.

    session:disconnect();

    return 0, {
        message = "Thanks for playing!",
        details = "You will be disconnected now.  Goodbye!"
    }

end
