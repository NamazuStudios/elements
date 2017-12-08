--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/23/17
-- Time: 12:20 PM
-- To change this template use File | Settings | File Templates.
--

local table = require "table"
local coroutine = require "coroutine"

local this = require "namazu.resource.this"
local detail = require "namazu.resource.detail"
local responsecode = require "namazu.response.code"
local resumereason = require "namazu.coroutine.resumereason"
local yieldinstruction = require "namazu.coroutine.yieldinstruction"

local resource = {}

--- Returns the ID of the current resource
-- Each resource has its own ID.  This simply returns the current resource id.
-- @return a string of the current resource id
function resource.id()
    return this:getId():asString()
end

local function process_result(reason, response)
    if reason == resumereason.NETWORK
    then
        return response, responsecode.OK
    elseif reason == resumereason.ERROR
    then
        return nil, response
    else
        return nil, responsecode.UNKNOWN
    end
end

--- Creates a resource at the supllied path
-- This will create a resource at the supplied path, specifying the module as well as any additional parameters which
-- will be read by the remote resource.  This returns both the network response as well as the resource ID.
--
-- This function yields until the response is available and must be invoked from within a system-managed coroutine.
--
-- @param module the module name
-- @param path the initial path of the module
-- @param attributes the resource attributes
-- @return the resource id
-- @return the response code
function resource.create(module, path, attributes, ...)
    detail.schedule_create(module, path, attributes, table.pack(...))
    local reason, response = coroutine.yield(yieldinstruction.INDEFINITELY)
    return process_result(reason, response)
end

--- Invokes a method (possibly remotely) on the supplied resource
-- This will invoke, possibly remotely, a method on the supplied resource_id.  All arguments passed to the method are
-- handed through the variadic arguments.
--
-- This function yields until the response is available and must be invoked from within a system-managed coroutine.
--
-- @param resource_id the resource id
-- @param method the name of the remote method
-- @return the response
-- @return the response code
function resource.invoke(resource_id, method, ...)
    detail.schedule_invoke(resource_id, method, table.pack(...))
    local reason, response = coroutine.yield(yieldinstruction.INDEFINITELY)
    return process_result(reason, response)
end

--- Invokes a method (possibly remotely) on the supplied resource
-- This will invoke, possibly remotely, a method on the supplied path.  All arguments passed to the method are handed
-- through the variadic arguments.
--
-- This function yields until the response is available and must be invoked from within a system-managed coroutine.
--
-- @param path the path of the resource
-- @param method the name of the remote method
-- @return the response
-- @return the response code
function resource.invoke_path(path, method, ...)
    detail.schedule_invoke_path(path, method, table.pack(...))
    local reason, response = coroutine.yield(yieldinstruction.INDEFINITELY)
    return process_result(reason, response)
end

--- Destroys a resourrce (possibly remotely)
-- This will permanently destory and unlink the resource with the supplied ID.  Once destroyed, the resource will no
-- longer accept method requests, and will no longer be indexable.
--
-- This function yields until the response is available and must be invoked from within a system-managed coroutine.
--
-- @param resource_id the path of the resource
-- @return The response code indicating the result of the request
function resource.destroy(resource_id)

    detail.schedule_destroy(resource_id)
    local reason, response = coroutine.yield(yieldinstruction.INDEFINITELY)

    if reason == resumereason.NETWORK
    then
        return response, responsecode.OK
    elseif reason == resumereason.ERROR
    then
        return response
    else
        return responsecode.UNKNOWN
    end

end

return resource
