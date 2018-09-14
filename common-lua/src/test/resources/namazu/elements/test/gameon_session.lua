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

    local session_client = gameon.session_client:authenticate(profile, device_os_type, build_type)

    assert(session_client ~= nil, "Expect non-nil session.  Got nil.")
    assert(session_client.sessionId == mock_response.sessionId, "Got " .. tostring(session_client.sessionId) .. ".  Expecting " ..  tostring(mock_response.sessionId) .. ".");
    assert(session_client.sessionApiKey == mock_response.sessionApiKey, "Got " .. tostring(session_client.apiKey) .. ".  Expecting " ..  tostring(mock_response.sessionApiKey) .. ".");
    assert(session_client.sessionExpirationDate == mock_response.sessionExpirationDate, "Got " .. tostring(session_client.sessionExpirationDate) .. ".  Expecting " ..  tostring(mock_response.sessionExpirationDate) .. ".");

end

function test.test_refresh_session(profile, device_os_type, build_type, session)

    local session_client = gameon.session_client:refresh(profile, device_os_type, build_type)

--    assert(session_Client ~= nil, "Expect non-nil session.  Got nil.")
--    assert(session_Client.sessionId == session.sessionId, "Got " .. tostring(session_Client.sessionId) .. ".  Expecting " ..  tostring(session.sessionId) .. ".");
--    assert(session_Client.sessionApiKey == session.sessionApiKey, "Got " .. tostring(session_Client.apiKey) .. ".  Expecting " ..  tostring(session.sessionApiKey) .. ".");
--    assert(session_Client.sessionExpirationDate == session.sessionExpirationDate, "Got " .. tostring(session_Client.sessionExpirationDate) .. ".  Expecting " ..  tostring(session.sessionExpirationDate) .. ".");

end

return test
