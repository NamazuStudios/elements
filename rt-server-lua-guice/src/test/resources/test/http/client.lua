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

    -- TODO Check status headers response

end

function client.get_all(base)
    -- TODO Make Test
end

function client.get_specific(base)
    -- TODO Make Test
end

function client.put(base)
    -- TODO Make Test
end

function client.delete(base)
    -- TODO Make Test
end

return client
