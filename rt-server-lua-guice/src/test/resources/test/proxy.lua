--
-- Created by IntelliJ IDEA.
-- User: ptwohig
-- Date: 3/21/22
-- Time: 2:27 PM
-- To change this template use File | Settings | File Templates.
--

local log = require "namazu.log"
local util = require "namazu.util"
local proxy = require "namazu.proxy"
local responsecode = require "namazu.response.code"
local this_resource = require "namazu.resource.this"

local test_proxy = {}

local function make_resource()

    local path = "/test/helloworld/" .. util.uuid()
    local resource, code = proxy.create("test.helloworld", path)

    if resource then
        log.info("Created resource " .. resource .. " (" .. code .. ") at path " .. path)
    else
        log.info("Failed to create clear(); (" .. code .. ") at path " .. path)
    end

    return resource, path

end

function test_proxy.test_create()

    local path = "/test/helloworld/" .. util.uuid()

    log.info("Making Resource.")
    local resource = proxy.create("test.helloworld", path)

    log.info("Made Resource.")
    assert(type(resource) == "proxy", "Expected proxy for resource_id got: " .. type(resource))

end

function test_proxy.test_invoke()

    local result, code
    local resource = make_resource()

    result, code = resource.knock_knock()
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    result, code = resource.identify("Interrupting Cow - Moo!")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and result, "Expected true.  Got : "  .. tostring(result))

end

function test_proxy.test_invoke_fail()

    local result, code
    local resource = make_resource()

    result, code = resource.knock_knock()
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. code)
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    result, code = resource.identify("Convex")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and not result, "Expected false.  Got : " .. tostring(result))

end

function test_proxy.test_invoke_path()

    local result, code
    local resource, path = make_resource()
    local resource_by_path = proxy.require_path(path)

    print("Knock Knock!")
    result, code = resource_by_path.knock_knock()
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. code)
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    print("Interrupting Cow!")
    result, code = resource_by_path.identify("Interrupting Cow - Moo!")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and result, "Expected true.  Got : "  .. tostring(result))

end

function test_proxy.test_invoke_path_fail()

    local result, code
    local rid, path = make_resource()
    local resource_by_path = proxy.require_path(path)

    print("Knock Knock!")
    result, code = resource_by_path.knock_knock()
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. code)
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    print("Convex")
    result, code = resource_by_path.identify("Convex!")
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and not result, "Expected false.  Got : " .. tostring(result))

end

function test_proxy.test_invoke_table()

    local result, code
    local resource = make_resource()

    result, code = resource.full_joke()
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    assert(type(result) == "table", "Expected \"table\" for type Got: " .. type(result))

    local count = 0
    for k,v in pairs(result)
    do
        count = count + 1
        print("Result key \"" .. tostring(k) .. "\" value " .. tostring(v))
    end

    assert(count == 3, "Expected specifically three results.  Got " .. tostring(count))
    assert(result.setup == "Knock Knock", "Expected setup to be \"Knock Knock\"  Got: " .. tostring(result.setup))
    assert(result.question == "Who's There?", "Expected setup to be \"Who's There?\"  Got: " .. tostring(result.question))
    assert(result.punchline == "Interrupting Cow - Moo!", "Expected setup to be \"Interrupting Cow - Moo!\"  Got: " .. tostring(result.punchline))

end

function test_proxy.test_invoke_array()

    local result, code
    local resource = make_resource()

    result, code = resource.full_joke_array()
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    assert(type(result) == "table", "Expected \"table\" for type Got: " .. type(result))

    assert(#result == 3, "Expected specifically three results.  Got " .. tostring(#result))
    assert(result[1] == "Knock Knock", "Expected setup to be \"Knock Knock\"  Got: " .. tostring(result[1]))
    assert(result[2] == "Who's There?", "Expected setup to be \"Who's There?\"  Got: " .. tostring(result[2]))
    assert(result[3] == "Interrupting Cow - Moo!", "Expected setup to be \"Interrupting Cow - Moo!\"  Got: " .. tostring(result[3]))

end

function test_proxy.test_destroy()

    local resource, path = make_resource()


    local result, code

    result, code = resource.knock_knock()
    assert(result == nil, "Expected nil result.  Got" .. tostring(result))
    assert(code == responsecode.RESOURCE_NOT_FOUND, "Expected Error Code " .. tostring(responsecode.RESOURCE_NOT_FOUND) .. " Got: " .. tostring(code));

    result, code = resource.invoke_path(path, "knock_knock")
    assert(result == nil, "Expected nil result.  Got" .. tostring(result))
    assert(code == responsecode.RESOURCE_NOT_FOUND, "Expected Error Code " .. tostring(responsecode.RESOURCE_NOT_FOUND) .. " Got: " .. tostring(code));

end

function test_proxy.test_list()

    local original = {}
    local prefix = util.uuid();
    -- Builds the listing

    for i = 1,5
    do
        local rid, path, code = make_resource(prefix);
        print("added " .. rid .. " at path " .. path)
        assert(code == responsecode.OK, "Expected OK response code got " .. tostring(code))
        original[path] = rid
    end

    local listing = proxy.list("test_list/*")
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


return test_proxy


