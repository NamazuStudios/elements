--
-- Created by IntelliJ IDEA.
-- User: ptwohig
-- Date: 3/17/22
-- Time: 10:53 AM
-- To change this template use File | Settings | File Templates.
--

local namazu_index = require "namazu.index"
local namazu_resource = require "namazu.resource"
local namazu_response_code = require "namazu.response.code"

local DISPATCH = "dispatch"
local PROXY_MAGIC = "$_namazu_proxy$"

local function new_proxy(metatable)

    metatable[PROXY_MAGIC] = true
    assert(metatable.resource_id or metatable.path, "Must have either path, resource id, or both.")
    assert(metatable.dispatch, "Must specify dispatch function.")

    function metatable:__index(method)
        local mt = getmetatable(self)
        local dispatch = mt.dispatch
        rawset(self, method, function(...) dispatch(self, method, ...) end)
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
    return namazu_resource.invoke_path(path, method, ...)
end

local function dispatch_resource_id(proxy, method, ...)
    local resource_id = getmetatable(proxy).resource_id
    return namazu_resource.invoke(resource_id, method, ...)
end

-- Public Interface Table

local namazu_proxy = {}

--- Constant to indicate path-based dispatch.
namazu_proxy.DISPATCH_PATH = dispatch_path

--- Constant to resource id based dispatch.
namazu_proxy.DISPATCH_RESOURCE_ID = dispatch_resource_id

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
    return new_proxy{path = path, dispatch=dispatch_path}
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
        return new_proxy{path=path, resource_id=resource_id, dispatch=dispatch_resource_id}, code
    else
        return nil, code
    end

end

--- If availble, allows for changing the dispatch behavior of this proxy
-- @param proxy the proxy
-- @param dispatch a function conforming to (proxy, method, ...) which performs the actual dispatch
function namazu_proxy.set_dispatch_type(proxy, dispatch)

    if not type(dispatch) == "function"
    then
        error("Must specify dispatch function.")
    end

    local metatable = getmetatable(proxy)

    if not metatable[PROXY_MAGIC]
    then
        error("Object is not a proxy.")
    end

    metatable.dispatch = dispatch

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

    local listings, code = namazu_index.list(path)

    if code ~= namazu_response_code.OK
    then
        return nil, code
    end

    local proxies = {}

    dispatch = dispatch or dispatch_resource_id

    for path, resource_id in pairs(listings)
    do
        local proxy = new_proxy{path=path, resource_id=resource_id, dispatch=dispatch_resource_id}
        table.insert(proxies, proxy)
    end

    return proxies, code

end

--- Gets the Path of a Proxy
-- This returns the path associated with the proxy instance. This is the path which was issued when the resource
-- was created or the used to create the proxy. The path is not always availble, such as when the proxy is created
-- with a resource id. In cases where path info is not available, then the function returns nil.
--
-- The function throws an error if the supplied object is not a proxy.
--
-- @proxy the proxy instance
-- @return the path, or nil
function namazu_proxy.get_path(proxy)

    local metatable = getmetatable(proxy)

    if not metatable[PROXY_MAGIC]
    then
        error("Object is not a proxy.")
    else
        return metatable["path"]
    end

end

--- Gets the Path of a Proxy
-- This returns the resource id associated with the proxy instance. The resource id is not always availble, such as when
-- the proxy is created with a path. In cases where resource id info is not available, then the function returns nil.
--
-- The function throws an error if the supplied object is not a proxy.
--
-- @proxy the proxy instance
-- @return the resource_id, or nil
function namazu_proxy.get_resource_id(proxy)

    local metatable = getmetatable(proxy)

    if not metatable[PROXY_MAGIC]
    then
        error("Object is not a proxy.")
    else
        return metatable["resource_id"]
    end

end

return namazu_proxy
