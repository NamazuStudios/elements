--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/15/17
-- Time: 10:28 PM
-- To change this template use File | Settings | File Templates.
--

local testjavamodule = require "test.java.module"

local javamodule = {}

function javamodule.test_hello_world()
    testjavamodule.hello_world()
end

function javamodule.test_return_hello_world()
    local val = testjavamodule.return_hello_world();
    assert(val == "Hello World!", "Expected 'Hello World!' but got " .. tostring(val))
end

function javamodule.test_overload_1()
    local val = testjavamodule.test_overload(42)
    assert(42 == val, "Expected 42 but got " .. tostring(val))
end

function javamodule.test_overload_2()
    local val = testjavamodule.test_overload(4, 2)
    assert(6 == val, "Expected 42 but got " .. tostring(val))

end

return javamodule
