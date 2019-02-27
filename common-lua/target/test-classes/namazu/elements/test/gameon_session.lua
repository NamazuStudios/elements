--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 7/12/18
-- Time: 11:36 AM
-- To change this template use File | Settings | File Templates.
--

local util = require "namazu.util"
local gameon = require "namazu.elements.amazon.gameon"

local test = {}

local function verify_match_client(session_client)
    local matchId = util.uuid()
    local match_client = session_client:match_client(matchId)
    assert(session_client ~= match_client, "Should not be same object.");
    assert(match_client.matchId == matchId, "Unexpected match ID: " .. tostring(matchId))
    assert(match_client.sessionId == session_client.sessionId, "Unexpected session ID: " .. tostring(session_client.sessionId))
    assert(match_client.sessionApiKey == session_client.sessionApiKey, "Unexpected session API Key: " .. tostring(session_client.sessionApiKey))
end

function test.test_authenticate_session(profile, device_os_type, app_build_type, mock_response)

    local session_client = gameon.session_client:authenticate_with_options{
        profile = profile,
        device_os_type = device_os_type,
        app_build_type = app_build_type
    }

    assert(session_client ~= nil, "Expect non-nil session.  Got nil.")
    assert(session_client.sessionId == mock_response.sessionId, "Got " .. tostring(session_client.sessionId) .. ".  Expecting " ..  tostring(mock_response.sessionId) .. ".");
    assert(session_client.sessionApiKey == mock_response.sessionApiKey, "Got " .. tostring(session_client.sessionApiKey) .. ".  Expecting " ..  tostring(mock_response.sessionApiKey) .. ".");
    assert(session_client.sessionExpirationDate == mock_response.sessionExpirationDate, "Got " .. tostring(session_client.sessionExpirationDate) .. ".  Expecting " ..  tostring(mock_response.sessionExpirationDate) .. ".");
    verify_match_client(session_client)

end

function test.test_refresh_session(profile, device_os_type, app_build_type, session)

    local session_client = gameon.session_client:refresh_with_options{
        profile = profile,
        device_os_type = device_os_type,
        app_build_type = app_build_type
    }

    assert(session_client ~= nil, "Expect non-nil session.  Got nil.")
    assert(session_client.sessionId == session.sessionId, "Got " .. tostring(session_client.sessionId) .. ".  Expecting " ..  tostring(session.sessionId) .. ".");
    assert(session_client.sessionApiKey == session.sessionApiKey, "Got " .. tostring(session_client.sessionApiKey) .. ".  Expecting " ..  tostring(session.sessionApiKey) .. ".");
    assert(session_client.sessionExpirationDate == session.sessionExpirationDate, "Got " .. tostring(session_client.sessionExpirationDate) .. ".  Expecting " ..  tostring(session.sessionExpirationDate) .. ".");
    verify_match_client(session_client)

end

function test.test_get_match_client()

    local sessionId = util.uuid()
    local sessionApiKey = util.uuid()
    local sessionExpirationDate = 420000

    local session_client = gameon.session_client:create{
        sessionId = sessionId,
        sessionApiKey = sessionApiKey,
        sessionExpirationDate = sessionExpirationDate
    }

    assert(session_client.sessionId == sessionId, "Unexpected session ID: " .. tostring(session_client.sessionId))
    assert(session_client.sessionApiKey == sessionApiKey, "Unexpected session API Key: " .. tostring(session_client.sessionApiKey))
    assert(session_client.sessionExpirationDate == sessionExpirationDate, "Unexpected session expiration: " .. tostring(session_client.sessionExpirationDate))

    verify_match_client(session_client)

end

return test
