--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 7/12/18
-- Time: 11:36 AM
-- To change this template use File | Settings | File Templates.
--

local gameon = require "namazu.elements.amazon.gameon"

local test = {}

function test.test_start_session(profile, device_os_type, build_type, mock_response)

    local status, session = gameon.session:authenticate(profile, device_os_type, build_type)

    assert(status == 200, "Expected 200 status.  Got " .. tostring(status))
    assert(session ~= nil, "Expect non-nil session.  Got nil.")
    assert(session.id == mock_response.sessionId, "Got " .. tostring(session.id) .. ".  Expecting " ..  tostring(mock_response.sessionId) .. ".");
    assert(session.api_key == mock_response.sessionApiKey, "Got " .. tostring(session.api_key) .. ".  Expecting " ..  tostring(mock_response.sessionApiKey) .. ".");
    assert(session.expires == mock_response.sessionExpirationDate, "Got " .. tostring(session.expires) .. ".  Expecting " ..  tostring(mock_response.sessionExpirationDate) .. ".");

end

return test
