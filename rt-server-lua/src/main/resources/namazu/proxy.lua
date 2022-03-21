--
-- Created by IntelliJ IDEA.
-- User: ptwohig
-- Date: 3/17/22
-- Time: 10:53 AM
-- To change this template use File | Settings | File Templates.
--

local namazu_index = require "namazu.index"
local namazu_resource = require "namazu.resource"

local DISPATCH = "dispatch"
local PROXY_MAGIC = "$_namazu_proxy$"

local function new_proxy(metatable)

    metatable[PROXY_MAGIC] = true
    assert(metatable.resource_id or metatable.path, "Must have either path, resource id, or both.")

    print("making Proxy")

    assert(metatable, "Must specify metatable.")
    assert(metatable.dispatch, "Must specify dispatch function.")

    function metatable:__index(method)
        local mt = getmetatable(self)
        local dispatch = mt[DISPATCH]
        rawset(self, method, function(...) mt.dispatch(self, method, ...) end)
        return rawget(self, method)
    end

    function metatable:__newindex(key)
        error("Cannot assign properties to proxy.")
    end

    local object = {}
    setmetatable(object, metatable)

    return object

end

local function dispatch_path(proxy, method, ...)
    local path = getmetatable(proxy).path
    assert(path, "Proxy does not have a path.")
    return namazu_resource.invoke_path(path, method, ...)
end

local function dispatch_resource_id(proxy, method, ...)
    local resource_id = getmetatable(proxy).resource_id
    assert(resource_id, "Proxy does not have a resource id.")
    return namazu_resource.invoke(resource_id, method, ...)
end

local namazu_proxy = {}

--- Creates a new proxy for the existing resource id
-- Creates a proxy which can make method calls to the remote proxy.
--
-- The supplied proxy will always attempt to dispatch method calls to
-- the resource id, returning the result from the remote invocation or any errors
-- at invocation time.
--
-- @param resource_id the resource id
-- @return the proxy instance
function namazu_proxy.require_resource_id(resource_id)
    return new_proxy{resource_id = resource_id, dispatch=dispatch_resource_id}
end

--- Creates a new proxy for the existing resource at the supplied path
-- Creates a proxy which can make method calls to the remote proxy.
--
-- The supplied proxy will always attempt to dispatch method calls to
-- the path, returning the result from the remote invocation or any errors
-- at invocation time.
--
-- @param path the resource id
-- @return the proxy instance
function namazu_proxy.require_path(path)
    return new_proxy{path = path, dispatch=dispatch_path }
end

--- Creates Resource and Returns its Proxy
-- This creates a new resource, fetches the resource id, and makes a proxy for the resource.
--
-- @param module the module name
-- @param path the initial path of the module
-- @param attributes the resource attributes
-- @return the proxy, or nil if the proxy was not created
-- @return the the response code
function namazu_proxy.create(module, path, attributes, ...)

    local resource_id, code = namazu_resource.create(module, path, attributes, ...)

    if resource_id then
        return new_proxy{
            path=path,
            resource_id=resource_id,
            dispatch=namazu_proxy.DISPATCH_RESOURCE_ID
        }
    else
        return nil, code
    end

end

--- If availble, allows for changing the dispatch behavior of this proxy
-- @param proxy the proxy
-- @param dispatch the
--
function namazu_proxy.set_dispatch_type(proxy, dispatch)
    getmetatable(proxy, namazu_proxy.DISPATCH, dispatch)
end

--- Lists all Resources and Returns Proxies to Interact with Them
-- THis method finds all resources matching the supplied path pattern and, for each found, makes a new proxy which can
-- be used to interact with the proxy on the remote side.
--
-- @param path the path, may be wildcard, to list all
-- @param dispatch the dispatch strategy to use on the returned proxies
-- @return a sequence containing a table containing paths mapped to resource_ids (or an empty table)
-- @return a response code
function namazu_proxy.list(path, dispatch)

    local proxies = {}
    local listings, code = namazu_index.list(path)

    if not dispatch
    then
        dispatch = namazu_proxy.DISPATCH_RESOURCE_ID
    end

    for path, resource_id in pairs(listings)
    do

        local proxy = new_proxy{
            path=path,
            resource_id=resource_id,
            dispatch=namazu_proxy.DISPATCH_RESOURCE_ID
        }

        table.insert(proxies, proxy)

    end

    return proxies, code

end

--- Removes Resource Associated with the Proxy
-- This function inspects the proxy and removes it, depending on how the proxy was created, this will either unlink
-- or it will destroy the resource associated with it.
--
-- @param proxy - the proxy
function namazu_proxy.remove(proxy)

end


--- Removes Resource Associated with the Proxy
-- This function inspects the proxy and removes it, depending on how the proxy was created, this will either unlink
-- or it will destroy the resource associated with it.
--
-- If
--
-- @param proxy - the proxy
function namazu_proxy.destroy(proxy)

end

return proxy
