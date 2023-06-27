local util = require "eci.util"
local resource = require "eci.resource"
local responsecode = require "eci.response.code"
local index = require "eci.index"
local log = require "eci.log"

local pass_table = {}

local ROOT_RESOURCE_PATH = "/test/echo/"

local function make_resource(subdirectory)

    local path = subdirectory == nil and
            ROOT_RESOURCE_PATH .. util.uuid() or
            ROOT_RESOURCE_PATH .. subdirectory .. util.uuid()

    local rid, code = resource.create("test.echo", path), path

    if rid == nil
    then
        log.error("Failed to create resource response {}", code)
    else
        log.error("Successfully created resource {} {}", rid, code)
    end

    print("Created resource " .. rid .. " (" .. code .. ") at path " .. path)
    return rid, code

end

function pass_table.pass_simple_array()

    local out = { "Hello!", "World!"}

    local result, code
    local rid = make_resource()

    for i = 1,5 do
        resource.invoke(rid, "commit", out)
        result, code = resource.invoke(rid, "echo")

        print("Got result " .. tostring(result) .. " with code " .. tostring(code))
        assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
        assert(result[1] == out[1], "Expected " .. tostring(out[1]) .. " response code.  Got: " .. tostring(result[1]))
        assert(result[2] == out[2], "Expected " .. tostring(out[1]) .. " response code.  Got: " .. tostring(result[1]))
    end

end

function pass_table.pass_simple_table()

    local out = { h = "Hello!", w = "World!"}

    local result, code
    local rid = make_resource()

    for i = 1,5 do
        resource.invoke(rid, "commit", out)
        result, code = resource.invoke(rid, "echo")

        print("Got result " .. tostring(result) .. " with code " .. tostring(code))
        assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
        assert(result["h"] == out["h"], "Expected " .. tostring(out["h"]) .. " response code.  Got: " .. tostring(result["h"]))
        assert(result["w"] == out["w"], "Expected " .. tostring(out["w"]) .. " response code.  Got: " .. tostring(result["w"]))
    end

end

function pass_table.pass_complex_array()

    local out = {
        { "Hello!", "World!" },
        { "¡Hola", "Mundo!"  },
        { { "¡Hola", "Mundo!" } }
    }

    local result, code
    local rid = make_resource()

    for i = 1, 5 do
        resource.invoke(rid, "commit", out)
        result, code = resource.invoke(rid, "echo")

        print("Got result " .. tostring(result) .. " with code " .. tostring(code))
        assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
        assert(result[1][1] == out[1][1], "Expected " .. tostring(out[1][1]) .. " response code.  Got: " .. tostring(result[1][1]))
        assert(result[1][2] == out[1][2], "Expected " .. tostring(out[1][2]) .. " response code.  Got: " .. tostring(result[1][2]))
        assert(result[2][1] == out[2][1], "Expected " .. tostring(out[2][1]) .. " response code.  Got: " .. tostring(result[2][1]))
        assert(result[2][2] == out[2][2], "Expected " .. tostring(out[2][1]) .. " response code.  Got: " .. tostring(result[2][1]))
    end

end

function pass_table.pass_complex_table()

    local out = {
        a = { a = "Hello!", b = "World!" },
        b = { a = "¡Hola",  b = "Mundo!" },
        c = { a = { h = "¡Hola", w = "Mundo!" } }
    }

    local result, code
    local rid = make_resource()

    for i = 1, 5 do
        resource.invoke(rid, "commit", out)
        result, code = resource.invoke(rid, "echo")

        print("Got result " .. tostring(result) .. " with code " .. tostring(code))
        assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
        assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
        assert(result["a"]["a"] == out["a"]["a"], "Expected " .. tostring(out["a"]["a"]) .. " response code.  Got: " .. tostring(result["a"]["a"]))
        assert(result["a"]["b"] == out["a"]["b"], "Expected " .. tostring(out["a"]["b"]) .. " response code.  Got: " .. tostring(result["a"]["b"]))
        assert(result["b"]["a"] == out["b"]["a"], "Expected " .. tostring(out["b"]["a"]) .. " response code.  Got: " .. tostring(result["b"]["a"]))
        assert(result["b"]["b"] == out["b"]["b"], "Expected " .. tostring(out["b"]["b"]) .. " response code.  Got: " .. tostring(result["b"]["b"]))
    end

end

function pass_table.pass_complex_table_to_multiple_resources()

    local out = {
        a = { a = "Hello!", b = "World!" },
        b = { a = "¡Hola",  b = "Mundo!" },
        c = { a = { h = "¡Hola", w = "Mundo!" } }
    }

    local result, code

    print("Creating tests")
    local resources_per_path = 5

    for test = 1, 5 do

        local subdirectory = tostring(test) .. util.uuid() .. "/"

        for i = 1, resources_per_path do
            result, code = make_resource(subdirectory)
            resource.invoke(result, "commit", out)
        end

        local list_path = ROOT_RESOURCE_PATH .. subdirectory .. "*"
        local listing = index.list(list_path)
        local num_listing = 0

        print("Got listing, now attempting to echo")

        for _, resource_id in pairs(listing) do

            num_listing = num_listing + 1

            result, code = resource.invoke(resource_id, "echo", out)

            print("Got result " .. tostring(result) .. " with code " .. tostring(code))
            assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
            assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
            assert(result["a"]["a"] == out["a"]["a"], "Expected " .. tostring(out["a"]["a"]) .. " response code.  Got: " .. tostring(result["a"]["a"]))
            assert(result["a"]["b"] == out["a"]["b"], "Expected " .. tostring(out["a"]["b"]) .. " response code.  Got: " .. tostring(result["a"]["b"]))
            assert(result["b"]["a"] == out["b"]["a"], "Expected " .. tostring(out["b"]["a"]) .. " response code.  Got: " .. tostring(result["b"]["a"]))
            assert(result["b"]["b"] == out["b"]["b"], "Expected " .. tostring(out["b"]["b"]) .. " response code.  Got: " .. tostring(result["b"]["b"]))

        end

        assert(num_listing == resources_per_path, "Expected " .. tostring(resources_per_path) .. " resources.  Got " .. tostring(num_listing) .. " instead")
    end
end

return pass_table