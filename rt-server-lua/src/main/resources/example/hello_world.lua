
local os = require "os"
local coroutine = require "coroutine"
local namazu_response = require "namazu.response"
local http_status = require "namazu.http.status"

local hello_world = {}

function hello_world.get(payload, request, session)

    response_payload = {
        string_property  = "Hello World!",
        number_property  = 4.2,
        boolean_property = false
    }

    -- It is possible to formulate a response using either the reserved built-in status codes or the HTTP status codes.
    -- This references the builtin status codes and allows the container to map to HTTP

    return namazu_response.formulate(namazu_response.code.OK, response_payload)

end

function hello_world.get_async(payload, request, session)

    response_payload = {
        string_property  = "Hello World!",
        number_property  = 4.2,
        boolean_property = false
    }

    print "Yielding immediately"
    coroutine.yield("IMMEDIATE")

    print "Yielding for one second"
    coroutine.yield("FOR", 1, "SECONDS")

    time = os.time() + 1
    print("Yielding until ", time, " (in seconds)")
    coroutine.yield("UNTIL_TIME", time, "SECONDS")

    print("Yielding until cron next cron for \"* * * ? * *\" (once every second)")
    coroutine.yield("UNTIL_NEXT", "* * * ? * *")

    print("Sending OK Response")

    -- It is possible to formulate a response using either the reserved built-in status codes or the HTTP status codes.
    -- This references the direct use of HTTP status codes

    return namazu_response.formulate(http_status.OK, response_payload)

end

return hello_world
