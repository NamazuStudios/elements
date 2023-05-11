--
-- Created by IntelliJ IDEA.
-- User: ptwohig
-- Date: 3/21/22
-- Time: 2:27 PM
-- To change this template use File | Settings | File Templates.
--

local util = require "eci.util"
local namazu_log = require "eci.log"
local namazu_proxy = require "eci.proxy"
local responsecode = require "eci.response.code"

local TEST_PREFIX = "test_proxy"

local test_proxy = {}

local function make_proxy()

    local path = string.format("%s/%s", TEST_PREFIX, util.uuid())
    local proxy, code = namazu_proxy.create("test.helloworld", path)

    if proxy then
        namazu_log.info("Created resource proxy with (Code: " .. tostring(code) .. ") at path " .. path)
    else
        namazu_log.info("Failed to create resource proxy with (Code: " .. tostring(code) .. ") at path " .. path)
    end

    return proxy, path

end

function test_proxy.test_create()
    local proxy = make_proxy()
    assert(proxy, "Expected on-nil proxy.")
end

function test_proxy.test_invoke()

    local result, code
    local proxy = make_proxy()

    result, code = proxy.knock_knock()
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    result, code = proxy.identify("Interrupting Cow - Moo!")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and result, "Expected true.  Got : "  .. tostring(result))

end

function test_proxy.test_invoke_path()

    local result, code
    local proxy, path = make_proxy()
    local proxy_by_path = namazu_proxy.require_path(path)

    result, code = proxy_by_path.knock_knock()
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

    result, code = proxy_by_path.identify("Interrupting Cow - Moo!")
    print("Got result " .. tostring(result) .. " with code " .. tostring(code))
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    assert(type(result) == "boolean" and result, "Expected true.  Got : "  .. tostring(result))

end

function test_proxy.test_list()

    local original = {}

    for i = 1,50
    do
        local path = string.format("%s/%s", TEST_PREFIX, util.uuid())
        local proxy, code = namazu_proxy.create("test.helloworld", path)
        assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    end

    local query = string.format("%s/*", TEST_PREFIX)
    local listings, code = namazu_proxy.list(query)
    assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
    assert(next(listings), "Expected non-zero result.")

    print("Got listings. Invoking each.")

    local total = #listings
    local invocation = 1

    for _,proxy in ipairs(listings)
    do

        local result, code

        print("Invocation " .. tostring(invocation) .. "/" .. tostring(total))
        invocation = invocation + 1

        result, code = proxy.knock_knock()
        print("Got result " .. tostring(result) .. " with code " .. tostring(code))
        assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
        assert(result == "Who's there?", "Expected \"Who's there?\"  Got: " .. result)

        result, code = proxy.identify("Interrupting Cow - Moo!")
        print("Got result " .. tostring(result) .. " with code " .. tostring(code))
        assert(code == responsecode.OK, "Expected " .. tostring(responsecode.OK) .. " response code.  Got: " .. tostring(code))
        assert(type(result) == "boolean" and result, "Expected true.  Got : "  .. tostring(result))

    end

end

return test_proxy
