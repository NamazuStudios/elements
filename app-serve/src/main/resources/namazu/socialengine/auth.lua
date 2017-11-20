--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/19/17
-- Time: 10:22 AM
-- To change this template use File | Settings | File Templates.
--

local attributes = require "namazu.resource.attributes"
local User = java.require "com.namazustudios.socialengine.model.User"
local Profile = java.require "com.namazustudios.socialengine.model.Profile"

local auth = {}

--- Fetches the current User
-- This fetches the current user executing the request.  This defers to the attributes set when the resoruce was
-- created.  If no user exists, then this returns the Unprivileged user.
function auth.user()
    return attributes:getAttributeOrDefault(User.USER_ATTRIBUTE, User:getUnprivileged())
end

--- Fetches the current Profile
-- This fetches the current profile executing the request.  This defers to the attributes set when the resoruce was
-- created.  If no profile exists, this returns nil
function auth.profile()
    return attributes:getAttribute(Profile.PROFILE_ATTRIBUTE)
end

return auth
