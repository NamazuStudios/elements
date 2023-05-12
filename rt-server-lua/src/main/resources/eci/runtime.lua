--
-- Created by IntelliJ IDEA.
-- User: ptwohig
-- Date: 3/24/22
-- Time: 7:23 PM
-- To change this template use File | Settings | File Templates.
--

local this = require "eci.resource.this"
local ResourceId = java.require "dev.getelements.elements.rt.id.ResourceId"

local runtime = {}

--- Gets the Instance ID of the current Resource
-- @return the instance id
function runtime.instance_id()
    return this:getId():getInstanceId():asString()
end

--- Gets the Application ID of the current Resource
-- @return the application id
function runtime.application_id()
    return this:getId():getNodeId():getApplicationId():asString()
end

--- Gets the Node ID of the current Resource
-- @return the node id
function runtime.node_id()
    return this:getId():getNodeId():asString()
end

--- Gets the Resource ID of the current Resource
-- @return the resource id
function runtime.resource_id()
    return this:getId():asString()
end

--- Gets a Node ID from a ResourceId
-- @return the resource id
function runtime.node_id_from_resource_id(resource_id)
    local rid = ResourceId:resourceIdFromString(resource_id)
    return rid:getNodeId():asString()
end

return runtime
