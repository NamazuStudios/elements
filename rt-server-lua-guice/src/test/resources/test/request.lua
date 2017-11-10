--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/19/17
-- Time: 8:54 PM
-- To change this template use File | Settings | File Templates.
--

local test_request = {}
local request = require "namazu.request"
local SimpleRequest = java.require "com.namazustudios.socialengine.rt.SimpleRequest"

local function do_formulate()
    return request.formulate_table{
        path = "/test/path",
        method = "test_method",
        payload = {},
        parameters = { foo = {"foo1", "foo2"}, bar = {"bar1", "bar2"} },
        headers    = { foo = {"foo1", "foo2"}, bar = {"bar1", "bar2"} },
        sequence = 0
    };
end

function test_request.test_formulate()

    local request = do_formulate()
    assert(request:getPayload() ~= nil, "expected non-nil payload")

    local header = request:getHeader();
    assert(header:getPath() == "/test/path", "Expected /test/path but got " .. header:getPath())
    assert(header:getMethod() == "test_method", "Expected test_method but got " .. header:getMethod())
    assert(header:getSequence() == 0, "expected 0 for sequence got " .. header:getSequence())

    local foo, bar

    foo = request:getParameters("foo")
    assert(type(foo) == "table", "Expected foo paramter to be a table got " .. type(foo))
    assert(#foo == 2, "Expected table length of 2 got " .. #foo)
    assert(foo[1] == "foo1", "Expected foo1 got " .. foo[1])
    assert(foo[2] == "foo2", "Expected foo2 got " .. foo[2])

    bar = request:getParameters("bar")
    assert(type(bar) == "table", "Expected foo paramter to be a table got " .. type(bar))
    assert(#bar == 2, "Expected table length of 2 got " .. #foo)
    assert(bar[1] == "bar1", "Expected bar1 got " .. bar[1])
    assert(bar[2] == "bar2", "Expected bar2 got " .. bar[2])

    foo = header:getHeaders("foo")
    assert(type(foo) == "table", "Expected foo paramter to be a table got " .. type(foo))
    assert(#foo == 2, "Expected table length of 2 got " .. #foo)
    assert(foo[1] == "foo1", "Expected foo1 got " .. foo[1])
    assert(foo[2] == "foo2", "Expected foo2 got " .. foo[2])

    bar = header:getHeaders("bar")
    assert(type(bar) == "table", "Expected foo paramter to be a table got " .. type(bar))
    assert(#bar == 2, "Expected table length of 2 got " .. #foo)
    assert(bar[1] == "bar1", "Expected bar1 got " .. bar[1])
    assert(bar[2] == "bar2", "Expected bar2 got " .. bar[2])

end

function test_request.test_unpack_headers()

    local req = do_formulate()

    local f1, f2 = request.unpack_headers(req, "foo")
    assert(f1 == "foo1", "Expected foo1 got " .. f1)
    assert(f2 == "foo2", "Expected foo2 got " .. f2)

    local b1, b2 = request.unpack_headers(req, "bar")
    assert(b1 == "bar1", "Expected bar1 got " .. b1)
    assert(b2 == "bar2", "Expected bar2 got " .. b2)

end

function test_request.test_unpack_parameters()

    local req = do_formulate()

    local f1, f2 = request.unpack_parameters(req, "foo")
    assert(f1 == "foo1", "Expected foo1 got " .. f1)
    assert(f2 == "foo2", "Expected foo2 got " .. f2)

    local b1, b2 = request.unpack_parameters(req, "bar")
    assert(b1 == "bar1", "Expected bar1 got " .. b1)
    assert(b2 == "bar2", "Expected bar2 got " .. b2)

end

function test_request.test_unpack_path_parameters()

    local builder = SimpleRequest:builder()

    local req = builder:path("/foo/bar/baz")
                       :parameterizedPath("/foo/{bar_id}/{baz_id}")
                       :build()

    local bar_id, baz_id = request.unpack_path_parameters(req)
--    assert(bar_id == "bar", "Expected bar got " .. bar_id)
--    assert(baz_id == "baz", "Expected baz got " .. baz_id)

end

return test_request
