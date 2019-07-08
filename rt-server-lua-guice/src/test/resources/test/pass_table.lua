local util = require "namazu.util"
local resource = require "namazu.resource"
local responsecode = require "namazu.response.code"

local pass_table = {}

local function make_resource()
    local path = "/test/echo/" .. util.uuid()
    local rid, code = resource.create("test.echo", path), path
    print("Created resource " .. rid .. " (" .. code .. ") at path " .. path)
    return rid, code
end

function pass_table.pass_simple_array()

    local out = { "Hello!", "World!"}

    local result, code
    local rid = make_resource()

    for i = 1,5 do
        result, code = resource.invoke(rid, "echo", out)
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
        result, code = resource.invoke(rid, "echo", out)
        print("Got result " .. tostring(result) .. " with code " .. tostring(code))
        assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
        assert(result["h"] == out["h"], "Expected " .. tostring(out["h"]) .. " response code.  Got: " .. tostring(result["h"]))
        assert(result["w"] == out["w"], "Expected " .. tostring(out["w"]) .. " response code.  Got: " .. tostring(result["w"]))
    end

end

function pass_table.pass_complex_array()

    local out = {
        { "Hello!", "World!" },
        { "¡Hola", "Mundo!"  }
    }

    local result, code
    local rid = make_resource()

    for i = 1, 5 do
        result, code = resource.invoke(rid, "echo", out)
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
        b = { a = "¡Hola",  b = "Mundo!" }
    }

    local result, code
    local rid = make_resource()

    for i = 1, 5 do
        result, code = resource.invoke(rid, "echo", out)
        print("Got result " .. tostring(result) .. " with code " .. tostring(code))
        assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
        assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
        assert(result["a"]["a"] == out["a"]["a"], "Expected " .. tostring(out["a"]["a"]) .. " response code.  Got: " .. tostring(result["a"]["a"]))
        assert(result["a"]["b"] == out["a"]["b"], "Expected " .. tostring(out["a"]["b"]) .. " response code.  Got: " .. tostring(result["a"]["b"]))
        assert(result["b"]["a"] == out["b"]["a"], "Expected " .. tostring(out["b"]["a"]) .. " response code.  Got: " .. tostring(result["b"]["a"]))
        assert(result["b"]["b"] == out["b"]["b"], "Expected " .. tostring(out["b"]["b"]) .. " response code.  Got: " .. tostring(result["b"]["b"]))
    end

end


return pass_table