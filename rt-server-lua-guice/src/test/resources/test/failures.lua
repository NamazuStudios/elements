--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/7/17
-- Time: 12:57 AM
-- To change this template use File | Settings | File Templates.
--

local failures = {}

function failures.lua_runtime_exception()
    local shouldbenil = nil
    print (shouldbenil.foo)
end

return failures
