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

--- Defines the Facebook OAuth Scheme
auth.facebook_oauth_scheme = "facebook_oauth"

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

--- Adds Facebook OAuth
-- Provided a security manifest, this will enable support for Facebook OAuth.  This will overwrite any Facebook OAuth
-- spec previously present in the manifest.  This will crate a header security definition if it is not defined.
-- @param the security manifest object
function auth.add_facebook_oauth_header(security_manifest)

    if (security_manifest["header"] == nil)
    then
        security_manifest.header = {}
    end

    security_manifest.header[auth.facebook_oauth_scheme] = {

        description = "Facebook OAuth Support",

        description = "Uses a combination Facebook Application ID in combination with an OAuth Token " ..
                "in order to perform API operations.  Must be specified in the format Facebook " ..
                "Authorization Facebook appid:token.  Failure to specify both app ID and token " ..
                "will result in a failed request.,",

        spec = {
            name = "Authorization",
            description = "The Standard HTTP Authorization Header",
            type = "string"
        }

    }

end

return auth
