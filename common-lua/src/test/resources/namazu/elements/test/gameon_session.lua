--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 7/12/18
-- Time: 11:36 AM
-- To change this template use File | Settings | File Templates.
--

local gameon = require "namazu.elements.amazon.gameon"

local test = {}

function test.test_authenticate_session(profile, device_os_type, build_type, mock_response)

    local status, session = gameon.session_client:authenticate(profile, device_os_type, build_type)

    assert(status == 200, "Expected 200 status.  Got " .. tostring(status))
    assert(session ~= nil, "Expect non-nil session.  Got nil.")
    assert(session.sessionId == mock_response.sessionId, "Got " .. tostring(session.sessionId) .. ".  Expecting " ..  tostring(mock_response.sessionId) .. ".");
    assert(session.apiKey == mock_response.sessionApiKey, "Got " .. tostring(session.apiKey) .. ".  Expecting " ..  tostring(mock_response.sessionApiKey) .. ".");
    assert(session.sessionExpirationDate == mock_response.sessionExpirationDate, "Got " .. tostring(session.sessionExpirationDate) .. ".  Expecting " ..  tostring(mock_response.sessionExpirationDate) .. ".");

end

return test
