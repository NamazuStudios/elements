--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 6/13/18
-- Time: 4:54 PM
-- To change this template use File | Settings | File Templates.
--

local upvalue

local persist_upvalue = {}

function persist_upvalue.set_upval(u)
    upvalue = u
end

function persist_upvalue.assert_upval()
    assert(upvalue ~= nil, "Expecting non-nil value for upvalue.");
    return upvalue;
end

return persist_upvalue;
