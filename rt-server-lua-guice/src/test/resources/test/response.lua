---
--- Created by keithhudnall.
--- DateTime: 12/7/17 5:09 PM
---

local util = require "namazu.util"
local resource = require "namazu.resource"

local responsecode = require "namazu.response.code"
local response = require "namazu.response"

local test_response = {}

function test_response.test_payload()

    local code = responsecode.OK
    local response_payload = { value = "PAYLOAD SUCCESS" }

    return response.formulate(code, response_payload), code
end

local function make_resource()

    local path = "/test/response/" .. util.uuid()
    local rid, code = resource.create("test.response", path)
    print("Created resource " .. rid .. " (" .. code .. ") at path " .. path)
    return rid, code

end

function test_response.test_simple_response()

    local result, code
    local rid = make_resource()

    result, code = resource.invoke(rid, "test_payload")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    assert(result:getPayload() ~= nil, "The payload is nil.")
    assert(result:getPayload().value == "PAYLOAD SUCCESS", "The payload value was not as expected - " .. tostring(result:getPayload().value))

end

return test_response