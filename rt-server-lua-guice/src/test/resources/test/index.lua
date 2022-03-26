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
local coroutine = require "coroutine"
local runtime = require "namazu.runtime"
local cluster = require "namazu.cluster"

local Path = java.require "com.namazustudios.socialengine.rt.Path"

local TEST_INDEX_PREFIX = "test_index"
local TEST_INDEX_PREFIX_ALIAS = "test_index_alias"

local test_index = {}

local function make_resource(context, prefix)

    -- Identifies the Path
    local uuid = util.uuid()
    local full = Path:new(context, {TEST_INDEX_PREFIX, prefix, uuid}):toString()
    local rid, code = resource.create("test.helloworld", full)
    print("Created resource " .. rid .. " (" .. code .. ") at path " .. full)

    -- Fully-qualifies the path based on the resource id
    local nid = runtime.node_id_from_resource_id(rid)
    local resolved = Path:new(nid, {TEST_INDEX_PREFIX, prefix, uuid}):toString()

    return rid, resolved, code

end

local function do_test_list(context)

    local original = {}
    local prefix = util.uuid();
    -- Builds the listing

    for i = 1,10
    do

        local rid, path, code = make_resource(context, prefix);
        assert(code == responsecode.OK, "Expected OK response code got " .. tostring(code))
        print("added " .. rid .. " at path " .. path)

        local node_id = runtime.node_id_from_resource_id(rid)
        original[path] = rid

    end

    local query = Path:new(context, {TEST_INDEX_PREFIX, prefix, "*"}):toString()
    local listing = index.list(query)

    assert(next(listing), "Expected at least one result.")
    assert(type(listing) == "table", "Expected table for listing but got " .. type(listing))

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

function test_index.test_list_local()
    do_test_list(nil)
end

function test_index.test_list_remote()
    local nid = runtime.node_id()
    do_test_list(nid)
end

function test_index.test_list_wildcard()
    do_test_list("*")
end

local function do_test_link(context)

    local response, rid, path, code
    rid, path, code = make_resource(context, "test");
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    local prefix = util.uuid();
    local alias = Path:new(context, {TEST_INDEX_PREFIX_ALIAS, prefix, util.uuid()}):toString()

    response, code = index.link(rid, alias)
    response, code = resource.invoke_path(alias, "knock_knock")
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))
    assert(response == "Who's there?", "Expected \"Who's There?\"  Got " .. tostring(response))

end

function test_index.test_link_local()
    do_test_link(nil)
end

function test_index.test_link_remote()
    local nid = runtime.node_id()
    do_test_link(nid)
end

local function do_test_link_path(context)

    local response, rid, path, code
    rid, path, code = make_resource(context, "test");
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    local prefix = util.uuid();
    local alias = Path:new(context, {TEST_INDEX_PREFIX_ALIAS, prefix, util.uuid()}):toString()

    response, code = index.link_path(path, alias)
    response, code = resource.invoke_path(alias, "knock_knock")
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))
    assert(response == "Who's there?", "Expected \"Who's There?\"  Got " .. tostring(response))

end

function test_index.test_link_path_local()
    do_test_link_path(nil)
end

function test_index.test_link_path_remote()
    local nid = runtime.node_id()
    do_test_link_path(nid)
end

local function do_test_unlink(context)

    local response, rid, original_rid, path, code, removed

    original_rid, path, code = make_resource(context, util.uuid());
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    response, code = resource.invoke_path(path, "save_resource")
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    local prefix = util.uuid();
    local alias = Path:new(context, {TEST_INDEX_PREFIX_ALIAS, prefix, util.uuid()}):toString()

    index.link(original_rid, alias)

    response, code = resource.invoke(original_rid, "knock_knock")
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    rid, removed, code = index.unlink(path)
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))
    assert(removed == false, "Expected resource wasn't removed.  It was.")

    response, code = resource.invoke_path(path, "knock_knock")
    assert(code == responsecode.RESOURCE_NOT_FOUND, "Expected response code " .. tostring(responsecode.RESOURCE_NOT_FOUND) .. " got " .. tostring(code))

    response, code = resource.invoke(rid, "knock_knock")
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    response, code = resource.invoke_path(alias, "knock_knock")
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

end

function test_index.test_unlink_local()
    do_test_unlink(nil)
end

function test_index.test_unlink_remote()
    local nid = runtime.node_id()
    do_test_unlink(nid)
end

local function test_unlink_and_destroy(context)

    local response, rid, original_rid, path, code, removed

    original_rid, path, code = make_resource(context, util.uuid());
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    response, code = resource.invoke_path(path, "save_resource")
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))

    local prefix = util.uuid();
    local alias = Path:new(context, {TEST_INDEX_PREFIX_ALIAS, prefix, util.uuid()}):toString()

    index.link(original_rid, alias)

    rid, removed, code = index.unlink(path)
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))
    assert(removed == false, "Expected resource wasn't removed.  It was.")

    rid, removed, code = index.unlink(alias)
    assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))
    assert(removed == true, "Expected resource was removed.  It wasn't.")

    response, code = resource.invoke(rid, "knock_knock")
    assert(code == responsecode.RESOURCE_NOT_FOUND, "Expected response code " .. tostring(responsecode.RESOURCE_NOT_FOUND) .. " got " .. tostring(code))

    response, code = resource.invoke_path(path, "knock_knock")
    assert(code == responsecode.RESOURCE_NOT_FOUND, "Expected response code " .. tostring(responsecode.RESOURCE_NOT_FOUND) .. " got " .. tostring(code))

    response, code = resource.invoke_path(alias, "knock_knock")
    assert(code == responsecode.RESOURCE_NOT_FOUND, "Expected response code " .. tostring(responsecode.RESOURCE_NOT_FOUND) .. " got " .. tostring(code))

end

function test_index.test_unlink_and_destroy_local()
    test_unlink_and_destroy(nil)
end

function test_index.test_unlink_and_destroy_remote()
    local nid = runtime.node_id()
    test_unlink_and_destroy(nid)
end

function test_index.test_link_yield_and_list()

    local original = {}
    local p1_id = util.uuid();
    local p2_id = util.uuid();
    -- Builds the listing

    for i = 1,10
    do
        local path = "test/" .. util.uuid()
        local rid, code = resource.create("test.helloworld", path)

        index.link(rid, p1_id .. "/" .. rid)
        index.link(rid, p2_id .. "/" .. rid)

        index.unlink(path)

        print("added " .. rid .. " at path " .. path)
        assert(code == responsecode.OK, "Expected OK response code got " .. tostring(code))
        original[path] = rid
    end

    -- Check player 1 listing
    local listing = index.list("test/" .. p1_id .. "/*")
    print(type(listing) == "table", "Expected table for listing but got " .. type(listing))

    for path, resource_id in pairs(listing)
    do
        print("resource "  .. tostring(path)  .. " -> " .. tostring(resource_id))
    end

    assert(#listing == #original, "Listing for player one failed to find all resources! Expected: " .. tostring(#original) .. " Found: " .. tostring(#listing))

    -- Check player 2 listing
    listing = index.list("test/" .. p2_id .. "/*")
    print(type(listing) == "table", "Expected table for listing but got " .. type(listing))

    for path, resource_id in pairs(listing)
    do
        print("resource "  .. tostring(path)  .. " -> " .. tostring(resource_id))
    end

    assert(#listing == #original, "Listing for player two failed to find all resources! Expected: " .. tostring(#original) .. " Found: " .. tostring(#listing))

end

return test_index
