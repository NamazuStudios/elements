--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 10/23/17
-- Time: 2:00 PM
-- To change this template use File | Settings | File Templates.
--

local test_util = {}

local util = require "eci.util"

function test_util.test_uuid()
    local UUID = java.require "java.util.UUID"

    local uuid = util.uuid()
    local parsed = UUID:fromString(uuid)
    assert(uuid == parsed:toString(), "Mismatching uuids " .. uuid .. " does not match " .. parsed:toString())

end

return test_util
