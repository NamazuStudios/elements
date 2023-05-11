--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 6/21/18
-- Time: 4:17 PM
-- To change this template use File | Settings | File Templates.
--

local coroutine = require "coroutine"
local namazu_coroutine = require "eci.coroutine"

local simple_yield = {}

function simple_yield.do_simple_yield()

    local co = coroutine.create(function()
        print "Yielding Indefinitely ..."
        coroutine.yield("INDEFINITELY")
        print "Resumed."
    end)

    return namazu_coroutine.start(co);

end

return simple_yield
