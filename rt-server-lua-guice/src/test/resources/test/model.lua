--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/17/17
-- Time: 6:34 PM
-- To change this template use File | Settings | File Templates.
--

local util = require "namazu.util"
local model = require "namazu.model"
local resource = require "namazu.resource"

local test_model = {}

-- Local invocations

function test_model.test_array()
    return model.array{}
end

function test_model.test_object()
    return model.object{ foo = model.array{} }
end

function test_model.test_array_default()
    return { "a", "b", "c" }
end

function test_model.test_object_default()
    return {}
end

function test_model.test_nil()
    return nil
end

-- Remote invocations.  These repeat all of the same, but it makes another instance to ensure that the values are
-- handed through the underlying service properly.


local function make_resource()
    -- Testception!
    local path = "/test/model/" .. util.uuid()
    local rid, code = resource.create("test.model", path), path
    print("Created resource " .. rid .. " (" .. code .. ") at path " .. path)
    return rid, code
end

function test_model.test_array_remote()
    local rid = make_resource()
    return resource.invoke(rid, "test_array")
end

function test_model.test_object_remote()
    local rid = make_resource()
    return resource.invoke(rid, "test_object")
end

function test_model.test_array_default_remote()
    local rid = make_resource()
    return resource.invoke(rid, "test_array_default")
end

function test_model.test_object_default_remote()
    local rid = make_resource()
    return resource.invoke(rid, "test_object_default")
end

function test_model.test_nil_remote()
    local rid = make_resource()
    return resource.invoke(rid, "test_nil")
end

return test_model
