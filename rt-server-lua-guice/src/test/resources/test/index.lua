--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/25/17
-- Time: 2:43 PM
-- To change this template use File | Settings | File Templates.
--

local util = require "namazu.util"
local index = require "namazu.index"
local resource = require "namazu.resource"
local responsecode = require "namazu.response.code"

local test_index = {}

local function make_resource(prefix)
    local path = prefix .. "/" .. util.uuid()
    local rid, code = resource.create("test.helloworld", path)
    print("Created resource " .. rid .. " (" .. code .. ") at path " .. path)
    return rid, path, code
end

function test_index.test_list()

    local original = {}
    local prefix = util.uuid();
    -- Builds the listing

    for i = 1,2
    do
        local rid, path, code = make_resource(prefix);
        print("added " .. rid .. " at path " .. path)
        assert(code == responsecode.OK, "Expected OK response code got " .. tostring(code))
        original[path] = rid
    end

    local listing = index.list("test_list/*")
    print(type(listing) == "table", "Expected table for listing but got " .. type(listing))

    for path, resource_id in pairs(listing)
    do
        print("resource "  .. tostring(path)  .. " -> " .. tostring(resource_id))
    end

    for path, resource_id in pairs(listing)
    do
        local original_rid = original[path]
        print("checking path " .. path)
        assert(original_rid == resource_id, "Path mismatch " .. tostring(original_rid) .. " does not match " .. tostring(resource_id))
    end

end

function test_index.test_link()

    local response, rid, path, code

    rid, path, code = make_resource("test");
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    local alias = "alias/" .. util.uuid()

    response, code = index.link(rid, alias)

    response, code = resource.invoke_path(alias, "knock_knock")
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))
    assert(response == "Who's there?", "Expected \"Who's There?\"  Got " .. tostring(response))

end

function test_index.test_link_path()

    local response, rid, path, code

    rid, path, code = make_resource("test");
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    local alias = "alias/" .. util.uuid()

    response, code = index.link_path(path, alias)

    response, code = resource.invoke_path(alias, "knock_knock")
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))
    assert(response == "Who's there?", "Expected \"Who's There?\"  Got " .. tostring(response))

end

function test_index.test_unlink()

    local response, rid, original_rid, path, code, removed

    original_rid, path, code = make_resource("test");
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    index.link(original_rid, "alias" .. "/" .. util.uuid())

    rid, removed, code = index.unlink(path)
    assert(original_rid == rid, "Expected resource id match")
    assert(removed == false, "Expected resource was removed.  It wasn't.")
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    response, code = resource.invoke_path(path, "knock_knock")
    assert(code == responsecode.RESOURCE_NOT_FOUND, "Expected response code " .. tostring(responsecode.RESOURCE_NOT_FOUND) .. " got " .. tostring(code))


end

function test_index.test_unlink_and_destroy()

    local response, rid, original_rid, path, code, removed

    original_rid, path, code = make_resource("test");
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    rid, removed, code = index.unlink(path)
    assert(original_rid == rid, "Expected resource id match")
    assert(removed, "Expected resource was removed.  It wasn't.")
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    response, code = resource.invoke_path(path, "knock_knock")
    assert(code == responsecode.RESOURCE_NOT_FOUND, "Expected response code " .. tostring(responsecode.RESOURCE_NOT_FOUND) .. " got " .. tostring(code))

end

return test_index
