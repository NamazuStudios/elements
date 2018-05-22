--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 5/21/18
-- Time: 6:32 PM
-- To change this template use File | Settings | File Templates.
--

local ioc = require "namazu.ioc.resolver"
local service = ioc:inject("com.namazustudios.socialengine.rt.lua.guice.TestJavaService")

local ioc_resolve = {}

function ioc_resolve.test_resolve()
    return service:helloWorld()
end

return ioc_resolve
