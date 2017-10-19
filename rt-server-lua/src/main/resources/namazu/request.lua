--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/19/17
-- Time: 1:32 AM
-- To change this template use File | Settings | File Templates.
--

local table = requre "table"
local request = {}

local Request       = java.require "com.namazustudios.socialengine.rt.Request"
local RequestHeader = java.require "com.namazustudios.socialengine.rt.RequestHeader"

local function formulate_request_header(path, method, headers, sequence)

    local methods = {}
    local headers = headers and headers or {}
    local sequence = sequence and sequence or RequestHeader.UNKNOWN_SEQUENCE

    function methods:getPath()
        return path
    end

    function methods:getMethod()
        return method
    end

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

    function methods:getSequence()
        return sequence
    end

    return RequestHeader:new(methods)

end

--- Formulates a Response from the provided parameters.
-- This will make a complete Request type  which is suitable for  use by the container, such as handing the Request to
-- various services for the puposes of dispatching to other Resource instances.
--
-- @param path - the request path
-- @param method - the request method
-- @param payload - the request payload
-- @param parameters - a table of tables containing the request parameters.  The keys are the paramter names
-- @param headers - table of tables containing the request headers.  The keys are the header names
-- @param sequence - the request sequence, may be nil or omitted to use the default of -1
-- @return a container Request
function request.formulate(path, method, payload, parameters, headers, sequence)

    local methods = {}
    local header = formulate_request_header(path, method, headers, sequence)

    function methods:getHeader()
        return header
    end

    function methods:getPayload()
        return payload
    end

    function methods:getParameterNames()

        local i = 0
        local names = {}

        for k, v in pairs(parameters) do
            i = i + 1
            names[i] = k
        end

        return names

    end

    function methods:getParameters(name)

        local value = parameters[name]
        if value == nil then return nil end

        if type(value) == "table" then
            return value
        else
            return { value }
        end

    end

    function methods:getParameterMap()
        return parameters
    end

    return Request:new(methods)

end

--- Formulates a Response providing the information in a table.
-- This allows the Response to be formulated using a table obviating the need to pass nil to various parameters before
-- formulating.  Each key corresponds to the parameters defined in the formulate function
--
-- @see request.formulate
-- @param optional the optional parameters for the response
-- @return a container Request
function request.formulate_table(path, method, optional)

    -- path, method, payload, parameters, headers, sequence

    return request.formulate(
        path,
        method,
        optional["payload"],
        optional["parameters"],
        optional["headers"],
        optional["sequence"]
    )

end

function request.unpack_header(request, header_name)
    -- TODO Write this code.
    return nil
end

function request.unpack_parameter(reqest, parameter_name)
    -- TODO Write this code.
    return nil
end

return request
