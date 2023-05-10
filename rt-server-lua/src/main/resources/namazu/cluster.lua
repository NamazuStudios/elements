--
-- Created by IntelliJ IDEA.
-- User: ptwohig
-- Date: 3/25/22
-- Time: 2:53 PM
-- To change this template use File | Settings | File Templates.
--

local ioc = require "namazu.ioc.resolver"
local registry = ioc:inject("dev.getelements.elements.rt.remote.RemoteInvokerRegistry")

local this = require "namazu.resource.this"

local Collectors = java.require "java.util.stream.Collectors"
local ApplicationId = java.require "dev.getelements.elements.rt.id.ApplicationId"

local cluster = {}

local function statuses_to_list_of_node_ids(statuses)

    local result = {}

    for _,status in ipairs(statuses)
    do
        table.insert(result, status:getNodeId():asString())
    end

    return result

end

--- Fetches All Known Known NodeIds in the Cluster
--
-- This fetches the latest snapshot of node ids for the entire cluster.
-- @return a list-style table of all node ids.
function cluster.get_node_ids()
    local statuses = registry:getAllRemoteInvokerStatuses()
    return statuses_to_list_of_node_ids(statuses)
end

--- Fetches the Best NodeId for The Current Application
--
-- This fetches the NodeId for the best node as determined by the cluster client. Typically this is the node
-- that last reported the lowest memory and CPU usage, but the details are completely up to the client implementation
-- and configuration.
--
-- @return a single node id for the best node
function cluster.get_best_node_id_for_application()
    local aid = this:getId():getNodeId():getApplicationId()
    return registry:getBestRemoteInvokerStatus(aid):getNodeId():asString()
end

--- Fetches the Best NodeId for The Specified Application
--
-- This fetches the NodeId for the best node as determined by the cluster client. Typically this is the node
-- that last reported the lowest memory and CPU usage, but the details are completely up to the client implementation
-- and configuration.
--
-- @return a single node id for the best node
function cluster.get_best_node_id_for_application_id(application_id)
    local aid = ApplicationId:valueOf(application_id)
    return registry:getBestRemoteInvokerStatus(aid):getNodeId():asString()
end

--- Fetches all NodeIds For the Current Application
--
-- This fetches all NodeIds for the current application.
--
-- @return a list-style table containing all nodes ids
function cluster.get_node_ids_for_application()
    local aid = this:getId():getNodeId():getApplicationId()
    local statuses = registry:getAllRemoteInvokerStatuses(aid)
    return statuses_to_list_of_node_ids(statuses)
end

--- Fetches all NodeIds For the Current Application
--
-- This fetches all NodeIds for the specified application application.
--
-- @return a list-style table containing all nodes ids
function cluster.get_node_ids_for_application_id(application_id)
    local aid = ApplicationId:valueOf(application_id)
    local statuses = registry:getAllRemoteInvokerStatuses(aid)
    return statuses_to_list_of_node_ids(statuses)
end

return cluster
