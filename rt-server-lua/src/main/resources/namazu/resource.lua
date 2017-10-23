--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/23/17
-- Time: 12:20 PM
-- To change this template use File | Settings | File Templates.
--

local table = require "table"
local detail = require "namazu.resource.detail"
local response_code = require "namazu.response.code"
local resume_reason = require "namazu.coroutine.resumereason"

local resource = {}

function resource.create(module, path, ...)

    local reason, response = detail.create(module, path, table.pack(...))

    if reason == resume_reason.NETWORK
    then
        return response, response_code.OK
    elseif reason == resume_reason.ERROR
    then
        return nil, response
    else
        return nil, response_code.UNKNOWN
    end

end

function resource.invoke(resource_id, method, ...)

    local reason, response = detail.invoke(resource_id, method, table.pack(...))

    if reason == resume_reason.NETWORK
    then
        return response, response_code.OK
    elseif reason == resume_reason.ERROR
    then
        return nil, response
    else
        return nil, response_code.UNKNOWN
    end

end

function resource.invoke_path(path, method, ...)

    local reason, response = detail.invoke_path(path, method, table.pack(...))

    if reason == resume_reason.NETWORK
    then
        return response, response_code.OK
    elseif reason == resume_reason.ERROR
    then
        return nil, response
    else
        return nil, response_code.UNKNOWN
    end

end

function resource.destroy(resource_id)

    local reason, response = detail.destroy(resource_id)

    if reason == resume_reason.NETWORK
    then
        return response, response_code.OK
    elseif reason == resume_reason.ERROR
    then
        return nil, response
    else
        return nil, response_code.UNKNOWN
    end

end

return resource
