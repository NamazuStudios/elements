--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 9/10/18
-- Time: 6:09 PM
-- To change this template use File | Settings | File Templates.
--

local java_lang_exception = java.require "java.lang.Exception"
local java_lang_illegal_state_exception = java.require "java.lang.IllegalStateException"
local java_lang_illegal_argument_exception = java.require "java.lang.IllegalArgumentException"

local obscure_exception_type = java.require "dev.getelements.elements.rt.lua.guice.ObscureException"

local util = require "eci.util"
local test_java_exceptions = require "test.java.exceptions"

local java_util_test = {}

function java_util_test.test_pcallx_happy()

    local result = util.java.pcallx(
        function()
            return "Hello world!"
        end,
        java_lang_illegal_state_exception, function(ex)
            assert(false, "Should never happen.")
        end,
        java_lang_illegal_argument_exception, function(ex)
            assert(false, "Should never happen.")
        end,
        java_lang_exception, function(ex)
            assert(false, "Should never happen.")
        end
    )

    assert("Hello world!" == result, "Unexpected result: ".. tostring(result))

end

function java_util_test.test_pcallx_handle_exception_1()

    local result = util.java.pcallx(
        function()
            test_java_exceptions.throw_type(java_lang_illegal_state_exception)
        end,
        java_lang_illegal_state_exception, function(ex)
            assert(java.instanceof(ex, java_lang_illegal_state_exception), "Unexpected exception: " .. tostring(ex))
            return "IllegalStateException"
        end,
        java_lang_illegal_argument_exception, function(ex)
            assert(false, "Should never happen.")
        end,
        java_lang_exception, function(ex)
            assert(false, "Should never happen.")
        end
    )

    assert("IllegalStateException" == result, "Unexpected result: ".. tostring(result))

end

function java_util_test.test_pcallx_handle_exception_2()

    local result = util.java.pcallx(
        function()
            test_java_exceptions.throw_type(java_lang_illegal_argument_exception)
        end,
        java_lang_illegal_state_exception, function(ex)
            assert(false, "Should never happen.")
        end,
        java_lang_illegal_argument_exception, function(ex)
            assert(java.instanceof(ex, java_lang_illegal_argument_exception), "Unexpected exception: " .. tostring(ex))
            return "IllegalArgumentException"
        end,
        java_lang_exception, function(ex)
            assert(false, "Should never happen.")
        end
    )

    assert("IllegalArgumentException" == result, "Unexpected result: ".. tostring(result))

end

function java_util_test.test_pcallx_handle_exception_3()
    local result = util.java.pcallx(
        function()
            test_java_exceptions.throw_type(java_lang_exception)
        end,
        java_lang_illegal_state_exception, function(ex)
        assert(false, "Should never happen.")
    end,
        java_lang_illegal_argument_exception, function(ex)
        assert(false, "Should never happen.")
    end,
        java_lang_exception, function(ex)
        return "Exception"
    end
    )

    assert("Exception" == result, "Unexpected result: ".. tostring(result))

end

function java_util_test.test_pcallx_unhandled()

    local result = util.java.pcallx(
        function()
            test_java_exceptions.throw_type(obscure_exception_type)
        end,
        java_lang_illegal_state_exception, function(ex)
            assert(false, "Should never happen.")
        end,
        java_lang_illegal_argument_exception, function(ex)
            assert(false, "Should never happen.")
        end
    )

    -- Should never reach this line of code because the exception should be re-thrown.
    assert(false, "Should never happen.")

end

return java_util_test
