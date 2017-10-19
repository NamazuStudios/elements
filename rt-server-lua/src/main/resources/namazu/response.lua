
require "table"

local Response       = java.require "com.namazustudios.socialengine.rt.Response"
local ResponseHeader = java.require "com.namazustudios.socialengine.rt.ResponseHeader"

local response = {}

--- Corresponds to the ResponseCode enumeration.  The set of builtin codes for returning responses.  See
-- com.namazustudios.socialengine.rt.ResponseCode for more information on this.
response.code = require "namazu.response.code"

local function forumulate_headers(code, headers, sequence)

    local methods = {}
    local headers = headers and headers or {}
    local sequence = sequence and sequence or ResponseHeader.UNKNOWN_SEQUENCE

    function methods:getHeaderNames()

        local i = 0
        local names = {}

        for k, v in pairs(headers) do
            i = i + 1
            names[i] = k
        end

        return names

    end

    function methods:getHeaders(name)

        local value = headers[name]
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

--- Formulates a container compatible Response
--
-- Methods that handle Request instances must return a Response.  If a Response is required, then this method can be
-- used to easily formulate a Response instance which is compatible with the underlying container.
--
-- Except for code, every argument is optional.  If not pecified, payload, headers, and sequence will be inferred using
-- default values.
--
-- @param code - the Response code
-- @param payload - the response payload
-- @param headers - table of tables containing the request headers.  The keys are the header names
-- @param sequence - the request sequence, may be nil or omitted to use the default of -1
-- @return a Response
function response.formulate(code, payload, headers, sequence)

    local methods = {}
    local headers = forumulate_headers(code, headers, sequence)

    function methods:getResponseHeader()
        return headers
    end

    function methods:getPayload()
        return payload
    end

    return Response:new(methods)

end

--- Formulates a response from a table
-- Accepts a table corresponding to the parameters of the formulate call.
--
-- @param code the response code (required)
-- @param optional a table containing the optional parameters
-- @return a Response
function response.formulate_optional(code, optional)
    return response.formulate(
        code,
        optional["payload"],
        optional["headers"],
        optional["sequence"]
    )
end

return response
