--
-- Created by IntelliJ IDEA.
-- User: ptwohig
-- Date: 3/25/22
-- Time: 4:11 PM
-- To change this template use File | Settings | File Templates.
--

local cluster = require "namazu.cluster"
local this = require "namazu.resource.this"

local NodeId = java.require "dev.getelements.elements.rt.id.NodeId"

local test_cluster = {}

function test_cluster.test_get_node_ids()

    local node_ids = cluster.get_node_ids()
    assert(type(node_ids) == "table", "Expected table but got " .. tostring(node_ids))
    assert(next(node_ids), "Expected non-empty table.")

    for _,node_id in pairs(node_ids)
    do
        NodeId:valueOf(node_id)
    end

end

function test_cluster.test_get_best_node_id_for_application()
    local node_id = cluster.get_best_node_id_for_application()
    NodeId:valueOf(node_id)
end

function test_cluster.test_get_best_node_id_for_application_id()
    local aid = this:getId():getNodeId():getApplicationId():asString()
    local node_id = cluster.get_best_node_id_for_application_id(aid)
    NodeId:valueOf(node_id)
end

function test_cluster.test_get_node_ids_for_application()

    local node_ids = cluster.get_node_ids_for_application()
    assert(type(node_ids) == "table", "Expected table but got " .. tostring(node_ids))
    assert(next(node_ids), "Expected non-empty table.")

    for _,node_id in pairs(node_ids)
    do
        NodeId:valueOf(node_id)
    end

end

function test_cluster.test_get_node_ids_for_application_id()

    local aid = this:getId():getNodeId():getApplicationId():asString()
    local node_ids = cluster.get_node_ids_for_application_id(aid)
    assert(type(node_ids) == "table", "Expected table but got " .. tostring(node_ids))
    assert(next(node_ids), "Expected non-empty table.")

    for _,node_id in pairs(node_ids)
    do
        NodeId:valueOf(node_id)
    end

end

return test_cluster
