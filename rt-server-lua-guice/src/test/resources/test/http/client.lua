--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 7/6/18
-- Time: 6:03 PM
-- To change this template use File | Settings | File Templates.
--

local log = require "namazu.log"

local client = {}

local namazu_http_client = require "namazu.http.client"

function client.post(base)

    log.info("Sending request {}", base)

    local status, headers, response = namazu_http_client.send{
        method = "GET",
        base = base,
        path = "simple"
    }

    assert(status  == 200, "Expected 200 but got " .. tostring(status))
    assert(headers ~= nil, "Expected non-nil headers.  Got nil instead.")
    assert(type(response) == "table", "Expected table for response.  Got " .. type(response))
    assert(#response == 0, "Expected empty table.  Got " .. tostring(#response) .. " entries instead.")
    assert(headers["Content-Length"][1] ~= nil, "Expected non-nil content length.");

    local status, headers, response = namazu_http_client.send{
        method = "POST",
        base = base,
        path = "simple",
        accept ="*/*",
        entity = {
            media_type = "application/json",
            value = {
                hello = "Hello!",
                world = "World!"
            }
        }
    }

    assert(status  == 200, "Expected 200 but got " .. tostring(status))
    assert(headers ~= nil, "Expected non-nil headers.  Got nil instead.")
    assert(type(response) == "table", "Expected table for response.  Got " .. type(response))
    assert(response.id ~= nil, "Expected non-nil id in response.  Got nil.")
    assert(response.hello == "Hello!", "Expected Hello! in response.  Got: " .. tostring(response.hello))
    assert(response.world == "World!", "Expected World! in response.  Got: " .. tostring(response.world))
    log.info("Got response id {}.  Hello: {}.  World: {}", response.id, response.hello, response.world)

end

function client.get_all(base)

    log.info("Sending request {}", base)

    local status, headers, response = namazu_http_client.send{
        method = "GET",
        base = base,
        path = "simple"
    }

    assert(status  == 200, "Expected 200 but got " .. tostring(status))
    assert(headers ~= nil, "Expected non-nil headers.  Got nil instead.")
    assert(type(response) == "table", "Expected table for response.  Got " .. type(response))
    assert(#response == 1, "Expected one entry in response.  Got " .. tostring(#response) .. " entries instead.")
    assert(headers["Content-Length"][1] ~= nil, "Expected non-nil content length.");

end


function client.get_all_query(base)

    log.info("Sending request {}", base)

    local status, headers, response = namazu_http_client.send{
        method = "GET",
        base = base,
        path = "simple",
        params = {
            hello = "Hello!",
            world = "World!"
        }
    }

    assert(status  == 200, "Expected 200 but got " .. tostring(status))
    assert(headers ~= nil, "Expected non-nil headers.  Got nil instead.")
    assert(type(response) == "table", "Expected table for response.  Got " .. type(response))
    assert(#response == 1, "Expected one entry in response.  Got " .. tostring(#response) .. " entries instead.")
    assert(headers["Content-Length"][1] ~= nil, "Expected non-nil content length.");

end

function client.get_specific(base)

    log.info("Sending request {}", base)

    local status, headers, response = namazu_http_client.send{
        method = "GET",
        base = base,
        path = "simple"
    }

    assert(status  == 200, "Expected 200 but got " .. tostring(status))
    assert(headers ~= nil, "Expected non-nil headers.  Got nil instead.")
    assert(type(response) == "table", "Expected table for response.  Got " .. type(response))
    assert(#response == 1, "Expected one entry in response.  Got " .. tostring(#response) .. " entries instead.")
    assert(headers["Content-Length"][1] ~= nil, "Expected non-nil content length.");

    local id = tostring(response[1].id)

    local status, headers, response = namazu_http_client.send{
        method = "GET",
        base = base,
        path = "simple/" .. id
    }

    assert(status  == 200, "Expected 200 but got " .. tostring(status))
    assert(headers ~= nil, "Expected non-nil headers.  Got nil instead.")
    assert(type(response) == "table", "Expected table for response.  Got " .. type(response))

    assert(response.id ~= nil, "Expected non-nil id in response.  Got nil.")
    assert(response.id == id, "Expected id " .. tostring(id) .. " in response.  Got " .. tostring(response.id))
    assert(response.hello == "Hello!", "Expected Hello! in response.  Got: " .. tostring(response.hello))
    assert(response.world == "World!", "Expected World! in response.  Got: " .. tostring(response.world))

end

function client.put(base)

    local status, headers, response = namazu_http_client.send{
        method = "GET",
        base = base,
        path = "simple"
    }

    assert(status  == 200, "Expected 200 but got " .. tostring(status))
    assert(headers ~= nil, "Expected non-nil headers.  Got nil instead.")
    assert(type(response) == "table", "Expected table for response.  Got " .. type(response))
    assert(#response == 1, "Expected one entry in response.  Got " .. tostring(#response) .. " entries instead.")
    assert(headers["Content-Length"][1] ~= nil, "Expected non-nil content length.");

    local id = tostring(response[1].id)

    local status, headers, response = namazu_http_client.send{
        method = "PUT",
        base = base,
        path = "simple/" .. id,
        entity = {
            media_type = "application/json",
            value = {
                hello = "Hello World!",
                world = "Hello World!"
            }
        }
    }

    assert(status  == 200, "Expected 200 but got " .. tostring(status))
    assert(headers ~= nil, "Expected non-nil headers.  Got nil instead.")
    assert(type(response) == "table", "Expected table for response.  Got " .. type(response))

    assert(response.id ~= nil, "Expected non-nil id in response.  Got nil.")
    assert(response.id == id, "Expected id " .. tostring(id) .. " in response.  Got " .. tostring(response.id))
    assert(response.hello == "Hello World!", "Expected Hello World! in response.  Got: " .. tostring(response.hello))
    assert(response.world == "Hello World!", "Expected Hello World! in response.  Got: " .. tostring(response.world))

end

function client.delete(base)

    local status, headers, response = namazu_http_client.send{
        method = "GET",
        base = base,
        path = "simple"
    }

    assert(status  == 200, "Expected 200 but got " .. tostring(status))
    assert(headers ~= nil, "Expected non-nil headers.  Got nil instead.")
    assert(type(response) == "table", "Expected table for response.  Got " .. type(response))
    assert(#response == 1, "Expected one entry in response.  Got " .. tostring(#response) .. " entries instead.")
    assert(headers["Content-Length"][1] ~= nil, "Expected non-nil content length.");

    local id = tostring(response[1].id)

    local status, headers, response = namazu_http_client.send{
        method = "DELETE",
        base = base,
        path = "simple/" .. id
    }

    assert(status  == 204, "Expected 200 but got " .. tostring(status))
    assert(headers ~= nil, "Expected non-nil headers.  Got nil instead.")
    assert(response == nil, "Expected nil for response.  Got " .. type(response) .. ": " .. tostring(response))

    local status, headers, response = namazu_http_client.send{
        method = "GET",
        base = base,
        path = "simple/" .. id
    }

    assert(status  == 404, "Expected 200 but got " .. tostring(status))
    assert(headers ~= nil, "Expected non-nil headers.  Got nil instead.")
    assert(response == nil, "Expected nil for response.  Got " .. type(response) .. ": " .. tostring(response))

end

return client
