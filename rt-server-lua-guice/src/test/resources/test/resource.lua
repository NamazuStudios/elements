--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/20/17
-- Time: 1:01 AM
-- To change this template use File | Settings | File Templates.
--

local util = require "namazu.util"
local resource = require "namazu.resource"
local responsecode = "namazu.response.code"

local test_resource = {}

local function make_resource()
    local path = "/test/helloworld/" .. util.uuid()
    return resource.create("test.helloworld", path), path
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
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. code)
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    result, code = resource.invoke(rid, "identify", "Interrupting Cow - Moo!")
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and result, "Expected true.  Got : "  .. tostring(result))

end

function test_resource.test_invoke_fail()

    local result, code
    local rid = make_resource()

    result, code = resource.invoke(rid, "knock_knock")
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. code)
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    result, code = resource.invoke(rid, "identify", "Convex")
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and not result, "Expected false.  Got : " .. tostring(result))

end


function test_resource.test_invoke_path()

    local result, code
    local rid, path = make_resource()

    result, code = resource.invoke_path(path, "knock_knock")
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. code)
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    result, code = resource.invoke_path(path, "identify", "Interrupting Cow - Moo!")
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and result, "Expected true.  Got : "  .. tostring(result))

end

function test_resource.test_invoke_path_fail()

    local result, code
    local rid, path = make_resource()

    result, code = resource.invoke_path(path, "knock_knock")
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. code)
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    result, code = resource.invoke_path(path, "identify", "Convex")
    assert(code == responsecode.OK, "Expected OK response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and not result, "Expected false.  Got : " .. tostring(result))

end

function test_resource.test_destroy()

    local rid = make_resource()
    resource.destroy(rid)

    local result, code = resource.invoke_path(path, "knock_knock")
    assert(result == nil, "Expected nil result.  Got" .. result)
    assert(code == responsecode.RESOURCE_NOT_FOUND, "Expected Error Code RESOURCE_NOT_FOUND.  Got: " .. code);

end

return test_resource
