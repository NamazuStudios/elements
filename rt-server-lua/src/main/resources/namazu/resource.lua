--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/23/17
-- Time: 12:20 PM
-- To change this template use File | Settings | File Templates.
--

local math = require "math"
local table = require "table"
local coroutine = require "coroutine"

local detail = require "namazu.resource.detail"
local responsecode = require "namazu.response.code"
local resumereason = require "namazu.coroutine.resumereason"
local yieldinstruction = require "namazu.coroutine.yieldinstruction"

local resource = {}

local function process_result(reason, response)
    if reason == resumereason.NETWORK
    then
        return response, responsecode.OK
    elseif reason == resumereason.ERROR
    then
        return nil, math.floor(response)
    else
        return nil, responsecode.UNKNOWN
    end
end

function resource.create(module, path, ...)
    detail.schedule_create(module, path, table.pack(...))
    local reason, response = coroutine.yield(yieldinstruction.INDEFINITELY)
    return process_result(reason, response)
end

function resource.invoke(resource_id, method, ...)
    detail.schedule_invoke(resource_id, method, table.pack(...))
    local reason, response = coroutine.yield(yieldinstruction.INDEFINITELY)
    return process_result(reason, response)
end

function resource.invoke_path(path, method, ...)
    detail.schedule_invoke_path(path, method, table.pack(...))
    local reason, response = coroutine.yield(yieldinstruction.INDEFINITELY)
    return process_result(reason, response)
end

function resource.destroy(resource_id)
    detail.schedule_destroy(resource_id)
    local reason, response = coroutine.yield(yieldinstruction.INDEFINITELY)
    return process_result(reason, response)
end

return resource
