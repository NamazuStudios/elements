--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 7/10/18
-- Time: 9:45 AM
-- To change this template use File | Settings | File Templates.
--

local http_client = require "namazu.http.client"
local ioc = require "namazu.ioc.resolver"
local configuration_dao = require "namazu.socialengine.dao.application.configuration.gameon"
local application_provider = ioc:provider("com.namazustudios.socialengine.model.application.Application")
local gameon_constants = require "namazu.elements.amazon._gameon.constants"

local session = {}

--- Raw Constructor
function session:new(session)
    session = session or {}
    session.__index = session
    setmetatable(session, self)
    return session
end

--- Creates a new Session
-- This creates a new session with the supplied credentials.  Unlike authenticate, this makes no network calls and is
-- useful for reconstituing a session with existing credentails.
--
-- @param id the session id
-- @param api_key the session api key
-- @param expires the time at which the session expires (stored only for reference, not used in implementation)
function session:create(id, api_key, expires)
    return session:new{id = id, api_key = api_key, expires = expires}
end

--- Authenticates a session
-- This calls the Amazon APIs to authenticate a new session for a particular user.  Upon successful response, a new
-- session is returned which can be used to manage calls to the Game On APIs.  This call may suspent the currently
-- running coroutine while the request is processed.
--
-- @param profile the user's profile
-- @param device_os_type the user's OS (eg Android or iOS)
-- @param app_build_type the build type (development vs production)
function session:authenticate(profile, device_os_type, app_build_type)

    local application = application_provider:get()
    local configuration = configuration_dao.get_default_configuration_for_application(application.id)

    local request = {
        method = "POST",
        base = gameon_constants.USER_BASE_URI,
        path = "/players/auth",
        headers = {
            [gameon_constants.API_KEY_HEADER] = configuration.publicApiKey
        },
        entity = {
            media_type = "application/json",
            value = {
                playerName = profile.displayName,
                appBuildType = app_build_type,
                deviceOSType = device_os_type
            }
        }
    }

    local status, headers, response = http_client.send(request)

    if (status == 200)
    then
        return status, session:create(response.sessionId, response.sessionApiKey, response.sessionExpirationDate)
    else
        return status, nil
    end

end

return session
