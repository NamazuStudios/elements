--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 9/10/18
-- Time: 5:47 PM
-- To change this template use File | Settings | File Templates.
--

local util_java = {}

local function pcallx_handle(ex, type, handler, ...)
    print ("Type: " .. tostring(type))
    if type == nil or handler == nil then
        error(ex)
    elseif java.instanceof(ex, type) then
        return handler(ex)
    else
        return pcallx_handle(ex, ...)
    end
end

--- Handles Java Exceptions
-- Handles Java exceptions by processing the first argument as a callable object (eg function) and remaining arguments
-- in pairs of type and handler functions.  This simulates the Java-style try-catch block without having to use native
-- Java code.
--
-- @param logic a function representing the block of code to run in protected mode
-- @param ... remaining argumetns in the form of [class0, function0(ex), [class1, function1(ex), ... [classN, functionN(ex)] ]]
function util_java.pcallx(logic, ...)

    local success, result = java.pcall(logic)

    if success then
        return result
    else
        return pcallx_handle(result, ...)
    end

end

return util_java
