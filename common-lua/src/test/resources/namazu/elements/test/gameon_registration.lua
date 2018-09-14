--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 9/14/18
-- Time: 2:58 PM
-- To change this template use File | Settings | File Templates.
--

local gameon = require "namazu.elements.amazon.gameon"

local test = {}

function test.test_refresh_registration(profile, registration)

    local registration_client = gameon.registration_client:refresh(profile)

    assert(registration_client ~= nil, "Expect non-nil session.  Got nil.")
    assert(registration_client.playerToken == registration.playerToken, "Got " .. tostring(registration_client.playerToken) .. ".  Expecting " ..  tostring(registration.playerToken) .. ".");
    assert(registration_client.externalPlayerId == registration.externalPlayerId, "Got " .. tostring(registration_client.externalPlayerId) .. ".  Expecting " ..  tostring(registration.externalPlayerId) .. ".");

end

return test
