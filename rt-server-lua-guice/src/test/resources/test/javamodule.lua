--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/15/17
-- Time: 10:28 PM
-- To change this template use File | Settings | File Templates.
--

local testjavamodulea = require "test.java.module.a"
local testjavamoduleb = require "test.java.module.a"

local javamodule = {}

local simple_model = java.require("com.namazustudios.socialengine.rt.lua.guice.rest.SimpleModel")

function javamodule.test_hello_world()
    testjavamodulea.hello_world()
    testjavamoduleb.hello_world()
end

function javamodule.test_return_hello_world()

    local val

    val = testjavamodulea.return_hello_world();
    assert(val == "Hello World!", "Expected 'Hello World!' but got " .. tostring(val))

    val = testjavamoduleb.return_hello_world();
    assert(val == "Hello World!", "Expected 'Hello World!' but got " .. tostring(val))

end

function javamodule.test_overload_1()
    local val

    val = testjavamodulea.test_overload(42)
    assert(42 == val, "Expected 42 but got " .. tostring(val))

    val = testjavamoduleb.test_overload(42)
    assert(42 == val, "Expected 42 but got " .. tostring(val))

end

function javamodule.test_overload_2()
    local val

    val = testjavamodulea.test_overload(4, 2)
    assert(6 == val, "Expected 42 but got " .. tostring(val))

    val = testjavamoduleb.test_overload(4, 2)
    assert(6 == val, "Expected 42 but got " .. tostring(val))

end

function javamodule.test_overload_fail()
    local val

    val = pcall(testjavamodulea.test_overload, simple_model:new())
    assert(not val, "Expected method failure.")

    val = pcall(testjavamodulea.test_overload, simple_model:new(), simple_model:new())
    assert(not val, "Expected method failure.")

end

function javamodule.test_java_pcall()

    local success, result

    success, result = pcall(testjavamodulea.throw_exception)
    assert(not success, "Expected unsuccessful call.")
    assert(result, "Expected non-nil result.")

    success, result = pcall(testjavamodulea.throw_exception, "test")
    assert(not success, "Expected unsuccessful call.")
    assert(result, "Expected non-nil result.")

end

return javamodule

