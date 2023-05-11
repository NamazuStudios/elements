--
-- Created by IntelliJ IDEA.
-- User: ptwohig
-- Date: 3/25/22
-- Time: 4:11 PM
-- To change this template use File | Settings | File Templates.
--

local runtime = require "eci.runtime"

local InstanceId = java.require "dev.getelements.elements.rt.id.InstanceId"
local NodeId = java.require "dev.getelements.elements.rt.id.NodeId"
local ApplicationId = java.require "dev.getelements.elements.rt.id.ApplicationId"
local ResourceId = java.require "dev.getelements.elements.rt.id.ResourceId"

local test_runtime = {}

function test_runtime.test_instance_id()
    local instance_id = runtime.instance_id()
    assert(instance_id, "Expected non-nil.")
    InstanceId:valueOf(instance_id)
end

function test_runtime.test_application_id()
    local application_id = runtime.application_id()
    assert(application_id, "Expected non-nil.")
    ApplicationId:valueOf(application_id)
end

function test_runtime.test_node_id()
    local node_id = runtime.node_id()
    assert(node_id, "Expected non-nil.")
    NodeId:valueOf(node_id)
end

function test_runtime.test_resource_id()
    local resource_id = runtime.resource_id()
    assert(resource_id, "Expected non-nil.")
    ResourceId:valueOf(resource_id)
end

function test_runtime.test_node_id_from_resource_id()
    local resource_id = runtime.resource_id()
    local node_id = runtime.node_id_from_resource_id(resource_id)
    assert(node_id, "Expected non-nil.")
end

return test_runtime
