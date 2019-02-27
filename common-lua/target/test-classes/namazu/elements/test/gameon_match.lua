--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 9/14/18
-- Time: 4:15 PM
-- To change this template use File | Settings | File Templates.
--

local gameon = require "namazu.elements.amazon.gameon"

local test = {}

function test.test_post_score(session_id, session_api_key, match_id, score)

    local client = gameon.match_client:new{
        sessionId = session_id,
        sessionApiKey = session_api_key,
        matchId = match_id
    }

    assert(score ~= nil, "Must provide score.")
    assert(client.matchId == match_id, "Match ID Mismatch")
    assert(client.sessionId == session_id, "Session ID Mismatch.")
    assert(client.sessionApiKey == session_api_key, "Session API Key Mismatch")

    client:submit_score(score)

end

return test
