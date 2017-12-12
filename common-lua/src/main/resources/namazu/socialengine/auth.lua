--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/19/17
-- Time: 10:22 AM
-- To change this template use File | Settings | File Templates.
--

local attributes = require "namazu.resource.attributes"
local User = java.require "com.namazustudios.socialengine.model.User"
local Profile = java.require "com.namazustudios.socialengine.model.profile.Profile"

local auth = {}

--- Defines the Facebook OAuth Scheme
auth.FACEBOOK_OAUTH_SCHEME = "facebook_oauth"

--- Fetches the current User
-- This fetches the current user executing the request.  This defers to the attributes set when the resoruce was
-- created.  If no user exists, then this returns the Unprivileged user.
--
-- @return the logged-in user making the request, or the unprivileged user.  Never nil
function auth.user()
    return attributes:getAttributeOrDefault(User.USER_ATTRIBUTE, User:getUnprivileged())
end

--- Fetches the current Profile
-- This fetches the current profile executing the request.  This defers to the attributes set when the resoruce was
-- created.  If no profile exists, this returns nil
-- @return the profile, or nil
function auth.profile()
    return attributes:getAttribute(Profile.PROFILE_ATTRIBUTE)
end


--- Makes the default security manifest
-- Returns a security manifest with all supported auth schemes pre-filled and documented.
--
-- @return a newly constructed security manifest
function auth.default_security_manifest()
    local security_manifest = {}
    -- TODO: This should actually read the application info and determine what profiles are active.  However, we don't
    -- TODO: pass this in at the manifest phase so we don't have acceess to that parameter.  Right now we just assume
    -- TODO: Facebook is active.

    auth.add_facebook_oauth_header(security_manifest)

    return security_manifest
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

    security_manifest.header[auth.FACEBOOK_OAUTH_SCHEME] = {

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
