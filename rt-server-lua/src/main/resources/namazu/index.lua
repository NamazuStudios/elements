--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/25/17
-- Time: 12:57 PM
-- To change this template use File | Settings | File Templates.
--

local coroutine = require "coroutine"

local detail = require "namazu.index.detail"
local responsecode = require "namazu.response.code"
local resumereason = require "namazu.coroutine.resumereason"
local yieldinstruction = require "namazu.coroutine.yieldinstruction"

local index = {}

local function process_listing(listings)

    local tabular_listing = {}

    for i, listing in ipairs(listings)
    do
        tabular_listing[listing:getPath():toNormalizedPathString()] = listing:getResourceId():asString()
    end

    return tabular_listing

end

--- Lists all ResourceIds matching a path
-- This executes a path query which may accept a wildcard path returning zero or more listings for resources.  The
-- return value is a table containing a mapping of path strings to resource_id strings.  Care must be taken when passing
-- a path to this function.  The remote will return all paths that match the supplied path.  Therefore, a large query
-- may consume considerable resources.  It is recommended that path schemes be crafted to return relatively small data
-- sets.
--
-- This function yields until the response is available and must be invoked from within a system-managed coroutine.
--
-- @param path the path, may be wildcard, to list
-- @return a sequence containing a table containing paths mapped to resource_ids (or an empty table)
-- @return a response code
function index.list(path)

    detail.schedule_list(path)
    local reason, response = coroutine.yield(yieldinstruction.INDEFINITELY)

    if reason == resumereason.NETWORK
    then
        return process_listing(response), responsecode.OK
    elseif reason == resumereason.ERROR
    then
        return {}, response
    else
        return {}, responsecode.UNKNOWN
    end

end

local function process_linkage(reason, response)
    if reason == resumereason.NETWORK
    then
        return responsecode.OK
    elseif reason == resumereason.ERROR
    then
        return response
    else
        return responsecode.UNKNOWN
    end
end

--- Links a ResourceId to a Path
-- Associates a resource id to a path, essentially creating an alias at the new path.  There may exist many paths
-- referencing a single resource_id but not the converse.  This is useful for generating collections or associations
-- among Resources in the cluster.
--
-- This function yields until the response is available and must be invoked from within a system-managed coroutine.
--
-- @param resource_id the resource id to link to the new path
-- @param path the destination path to link
-- @return the response code indicating if the link was successful or not
function index.link(resource_id, path)
    detail.schedule_link(resource_id, path)
    local reason, response = coroutine.yield(yieldinstruction.INDEFINITELY)
    return process_linkage(reason, response)
end

--- Links a Path to a Path
-- Associates a source path to a destination path, essentially creating an alias at the new path.  There may exist many
-- This is useful for generating collections or associations among Resources in the cluster.
--
-- This function yields until the response is available and must be invoked from within a system-managed coroutine.
--
-- @param source the source path
-- @param destination the destination path
-- @return the response code indicating if the link was successful or not
function index.link_path(source, destination)
    detail.schedule_link_path(source, destination)
    local reason, response = coroutine.yield(yieldinstruction.INDEFINITELY)
    return process_linkage(reason, response)
end

--- Unlinks a path to its associated ResourceId
-- Removes a previous association at a specific path.  When all paths pointing to a resource are removed, then the
-- cluster will remove and destroy the resource.  In this scenario, this will have the same effect as destroying the
-- the resource using its id.
--
-- This function yields until the response is available and must be invoked from within a system-managed coroutine.
--
-- @param path the path to unlink
-- @return the affected resource id
-- @return a boolean indicating if it was destroyed
-- @return the actual network response code
function index.unlink(path)

    detail.schedule_unlink(path)

    local reason, response = coroutine.yield(yieldinstruction.INDEFINITELY)

    if reason == resumereason.NETWORK
    then
        return response:getResourceId():asString(), response:isDestroyed(), resumereason.OK
    elseif reason == resumereason.ERROR
    then
        return nil, false, response
    else
        return nil, false, responsecode.UNKNOWN
    end

end

return index
