--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 7/12/18
-- Time: 11:36 AM
-- To change this template use File | Settings | File Templates.
--

local gameon = require "namazu.elements.amazon.gameon"

local test = {}

function test.test_start_session(profile, api_key)

    local status, session = gameon.session:authenticate(profile, "android", api_key)

    assert(status == 200, "Expected 200 status.  Got " .. tostring(status))
    assert(session ~= nil, "Expect non-nil session.  Got nil.")

end

return test
