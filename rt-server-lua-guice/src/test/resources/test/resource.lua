--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/20/17
-- Time: 1:01 AM
-- To change this template use File | Settings | File Templates.
--

local util = require "namazu.util"
local resource = require "namazu.resource"
local responsecode = require "namazu.response.code"

local test_resource = {}

local function make_resource()
    local path = "/test/helloworld/" .. util.uuid()
    local rid, code = resource.create("test.helloworld", path), path
    print("Created resource " .. rid .. " (" .. code .. ") at path " .. path)
    return rid, code
end

function test_resource.test_create()

    local ResourceId = java.require "com.namazustudios.socialengine.rt.ResourceId"

    local rid = make_resource()
    assert(type(rid) == "string", "Expected string for resource_id got: " .. type(rid))
    ResourceId:new(rid);

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
    assert(type("result") == "table", "Expected \"table\" for type Got: " .. type(result))

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

return test_resource
