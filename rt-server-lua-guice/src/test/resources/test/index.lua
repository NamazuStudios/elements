--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/25/17
-- Time: 2:43 PM
-- To change this template use File | Settings | File Templates.
--

local util = require "eci.util"
local index = require "eci.index"
local resource = require "eci.resource"
local responsecode = require "eci.response.code"
local coroutine = require "coroutine"
local runtime = require "eci.runtime"
local cluster = require "eci.cluster"

local Path = java.require "dev.getelements.elements.rt.Path"

local TEST_INDEX_PREFIX = "test_index"
local TEST_INDEX_PREFIX_ALIAS = "test_index_alias"
local TEST_INDEX_PREFIX_UNLINK_YIELD = "test_index_unlink_yield"

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

local function do_test_link_yield_and_list(context)

    local original = {}
    local prefix = util.uuid()
    local player_1 = util.uuid()
    local player_2 = util.uuid()

    -- Builds the listing

    for i = 1,10
    do

        local rid, path, code = make_resource(context, prefix);
        assert(code == responsecode.OK, "Expected OK response code got " .. tostring(code))
        print("Added " .. rid .. " at path " .. path)

        local nid = runtime.node_id_from_resource_id(rid)
        local node_id = runtime.node_id_from_resource_id(rid)
        local player_1_full = Path:new(nid, {TEST_INDEX_PREFIX_UNLINK_YIELD, prefix, player_1, util.uuid()}):toString()
        local player_2_full = Path:new(nid, {TEST_INDEX_PREFIX_UNLINK_YIELD, prefix, player_2, util.uuid()}):toString()

        original[player_1_full] = rid
        original[player_2_full] = rid

        index.link(rid, player_1_full)
        index.link(rid, player_2_full)
        index.unlink(path)

    end

    local function do_test(query)

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
            print("Checking path " .. path)
            assert(original_rid == resource_id, "Path mismatch " .. tostring(original_rid) .. " does not match " .. tostring(resource_id))

            local response, code = resource.invoke(original_rid, "knock_knock")
            assert(code == responsecode.OK, "Expected response code " .. tostring(responsecode.OK) .. " got " .. tostring(code))
        end

    end

    local query

    query = Path:new(context, {TEST_INDEX_PREFIX_UNLINK_YIELD, prefix, "*"}):toString()
    print("Testing query: " .. query)
    do_test(query)

    query = Path:new(context, {TEST_INDEX_PREFIX_UNLINK_YIELD, prefix, player_1, "*"}):toString()
    print("Testing query: " .. query)
    do_test(query)

    query = Path:new(context, {TEST_INDEX_PREFIX_UNLINK_YIELD, prefix, player_2, "*"}):toString()
    print("Testing query: " .. query)
    do_test(query)

end

function test_index.test_link_yield_and_list_local()
    do_test_link_yield_and_list(nil)
end

function test_index.test_link_yield_and_list_remote()
    local nid = runtime.node_id()
    do_test_link_yield_and_list(nid)
end

function test_index.test_link_yield_and_list_wildcard()
    do_test_link_yield_and_list("*")
end

return test_index
