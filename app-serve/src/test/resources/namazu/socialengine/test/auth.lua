--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/25/17
-- Time: 10:16 PM
-- To change this template use File | Settings | File Templates.
--

local auth = require "namazu.socialengine.auth"

local test_auth = {}

function test_auth.test_facebook_security_manifest()

    local manifest = auth.default_security_manifest();
    assert(manifest ~= nil, "Expected non-nil security manifest.")

    local header = manifest["header"]
    assert(header ~= nil, "Expected non-nil security manifest.")

    local facebook_oauth_scheme = header[auth.FACEBOOK_OAUTH_SCHEME]
    assert(facebook_oauth_scheme ~= nil, "Expected non-nil Facebook oauth scheme")

    local description, spec

    description, spec = facebook_oauth_scheme["description"], facebook_oauth_scheme["spec"]
    assert(spec ~= nil, "Facebook spec nil.")
    assert(description ~= nil, "Facebook description nil.")

    assert(spec["name"] == "Authorization", "Expected 'Authorization.'  Got: " .. tostring(spec["name"]))
    assert(spec["description"] == "The Standard HTTP Authorization Header", "Got: " .. tostring(spec["description"]))
    assert(spec["type"] == "string", "Expected 'string'.  Got: " .. tostring(spec["type"]))

end

return test_auth
