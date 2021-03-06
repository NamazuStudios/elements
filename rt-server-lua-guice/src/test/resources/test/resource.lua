--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/20/17
-- Time: 1:01 AM
-- To change this template use File | Settings | File Templates.
--

local log = require "namazu.log"
local util = require "namazu.util"
local resource = require "namazu.resource"
local responsecode = require "namazu.response.code"
local this_resource = require "namazu.resource.this"

local test_resource = {}

local function make_resource()

    local path = "/test/helloworld/" .. util.uuid()
    local rid, code = resource.create("test.helloworld", path), path

    if rid then
        log.info("Created resource " .. rid .. " (" .. code .. ") at path " .. path)
    else
        log.info("Failed to create clear(); (" .. code .. ") at path " .. path)
    end

    return rid, code

end

function test_resource.test_create()

    local path = "/test/helloworld/" .. util.uuid()
    local ResourceId = java.require "com.namazustudios.socialengine.rt.id.ResourceId"

    log.info("Making Resource")
    local rid = resource.create("test.helloworld", path)

    log.info("Made Resource")
    assert(type(rid) == "string", "Expected string for resource_id got: " .. type(rid))

    log.info("Resource id {}", rid)
    local parsed = ResourceId:resourceIdFromString(rid);

    log.info("Parsed ResourceId {}", parsed)

end

function test_resource.test_invoke()

    local result, code
    local rid = make_resource()

    result, code = resource.invoke(rid, "knock_knock")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    result, code = resource.invoke(rid, "identify", "Interrupting Cow - Moo!")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and result, "Expected true.  Got : "  .. tostring(result))

end

function test_resource.test_invoke_fail()

    local result, code
    local rid = make_resource()

    result, code = resource.invoke(rid, "knock_knock")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. code)
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    result, code = resource.invoke(rid, "identify", "Convex")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and not result, "Expected false.  Got : " .. tostring(result))

end

function test_resource.test_invoke_path()

    local result, code
    local rid, path = make_resource()

    print("Knock Knock!")
    result, code = resource.invoke_path(path, "knock_knock")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. code)
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    print("Interrupting Cow!")
    result, code = resource.invoke_path(path, "identify", "Interrupting Cow - Moo!")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and result, "Expected true.  Got : "  .. tostring(result))

end

function test_resource.test_invoke_path_fail()

    local result, code
    local rid, path = make_resource()

    print("Knock Knock!")
    result, code = resource.invoke_path(path, "knock_knock")
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. code)
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    print("Convex")
    result, code = resource.invoke_path(path, "identify", "Convex")
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and not result, "Expected false.  Got : " .. tostring(result))

end

function test_resource.test_invoke_table()

    local result, code
    local rid = make_resource()

    result, code = resource.invoke(rid, "full_joke")
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

function test_resource.test_invoke_array()

    local result, code
    local rid = make_resource()

    result, code = resource.invoke(rid, "full_joke_array")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    assert(type(result) == "table", "Expected \"table\" for type Got: " .. type(result))

    assert(#result == 3, "Expected specifically three results.  Got " .. tostring(#result))
    assert(result[1] == "Knock Knock", "Expected setup to be \"Knock Knock\"  Got: " .. tostring(result[1]))
    assert(result[2] == "Who's There?", "Expected setup to be \"Who's There?\"  Got: " .. tostring(result[2]))
    assert(result[3] == "Interrupting Cow - Moo!", "Expected setup to be \"Interrupting Cow - Moo!\"  Got: " .. tostring(result[3]))

end

function test_resource.test_destroy()

    local rid, path = make_resource()
    resource.destroy(rid)

    local result, code

    result, code = resource.invoke(rid, "knock_knock")
    assert(result == nil, "Expected nil result.  Got" .. tostring(result))
    assert(code == responsecode.RESOURCE_NOT_FOUND, "Expected Error Code " .. tostring(responsecode.RESOURCE_NOT_FOUND) .. " Got: " .. tostring(code));

    result, code = resource.invoke_path(path, "knock_knock")
    assert(result == nil, "Expected nil result.  Got" .. tostring(result))
    assert(code == responsecode.RESOURCE_NOT_FOUND, "Expected Error Code " .. tostring(responsecode.RESOURCE_NOT_FOUND) .. " Got: " .. tostring(code));

end

function test_resource.test_this()
    assert(this_resource ~= nil, "Expected non-nil value for this resource.")
end

return test_resource
