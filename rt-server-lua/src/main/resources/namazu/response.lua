
require "table"

local Response       = java.require "com.namazustudios.socialengine.rt.Response"
local ResponseHeader = java.require "com.namazustudios.socialengine.rt.ResponseHeader"

local namazu_response = {}
namazu_response.code = require "namazu.response.code"

local function forumulate_headers(code, headers, sequence)

    print "Formulating headers."

    headers = headers and headers or {}
    sequence = sequence and sequence or ResponseHeader.UNKNOWN_SEQUENCE

    methods = {}

    function methods:getHeaderNames()

        i = 0
        names = {}

        for k, v in pairs(headers) do
            i = i + 1
            names[i] = k
        end

        return names

    end

    function methods:getHeaders(name)

        value = headers[name]
        if value == nil then return nil end

        if type(value) == "table" then
           return value
        else
            return { value }
        end

    end

    function methods:getCode()
        return code
    end

    function methods:getSequence()
        return sequence
    end

    return ResponseHeader:new(methods)

end

-- Methods that handle Request instances must return a Response.  If a Response is required, then this method can be
-- used to easily formulate a Response instance which is compatible with the underlying container.
--
-- Except for code, every argument is optional.  If not pecified, payload, headers, and sequence will be inferred using
-- default values.

function namazu_response.formulate(code, payload, headers, sequence)

    print "Formulating response"

    headers = forumulate_headers(code, headers, sequence)

    methods = {}

    function methods:getResponseHeader()
        return headers
    end

    function methods:getPayload()
        return payload
    end

    return Response:new(methods)

end

return namazu_response
