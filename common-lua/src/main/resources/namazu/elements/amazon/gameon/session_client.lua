--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 7/10/18
-- Time: 9:45 AM
-- To change this template use File | Settings | File Templates.
--

local ioc = require "namazu.ioc.resolver"
local http_client = require "namazu.http.client"

local application_provider = ioc:provider("com.namazustudios.socialengine.model.application.Application")
local configuration_dao = require "namazu.socialengine.dao.application.configuration.gameon"

local gameon_constants = require "namazu.elements.amazon.gameon.constants"
local gameon_session = java.require "com.namazustudios.socialengine.model.gameon.game.GameOnSession"
local gameon_session_dao = require "namazu.elements.dao.gameon.session"
local gameon_registration_client = require "namazu.elements.amazon.gameon.registration_client"

local session_not_found_exception = java.require "com.namazustudios.socialengine.exception.gameon.GameOnSessionNotFoundException"

local session_client = {}

--- The base path for session APIs
session_client.PATH = "/players/session"

--- Raw Constructor
function session_client:new(session_client)
    session_client = session_client or {}
    session_client.__index = session_client
    setmetatable(session_client, self)
    return session_client
end

--- Creates a new Session
-- This creates a new session with the supplied credentials.  Unlike authenticate, this makes no network calls and is
-- useful for reconstituing a session with existing credentails.
--
-- @param id the session id
-- @param api_key the session api key
-- @param expires the time at which the session expires (stored only for reference, not used in implementation)
function session_client.create(session)
    return session_client:new{
        id = session.id,
        deviceOSType = tostring(session.deviceOSType),
        appBuildType = tostring(session.appBuildType),
        sessionId = session.id,
        sessionApiKey = session.sessionApiKey,
        sessionExpirationDate = session.sessionExpirationDate,
        profile = session.profile
    }
end

--- Authenticates a session (Class Method)
-- This calls the Amazon APIs to authenticate a new session for a particular user.  Upon successful response, a new
-- session is returned which can be used to manage calls to the Game On APIs.  This call may suspent the currently
-- running coroutine while the request is processed.
--
-- Note, the returned session is not stored in the database.
--
-- @param profile the user's profile
-- @param device_os_type the user's OS (eg Android or iOS)
-- @param app_build_type the build type (development vs production)
-- @return a freshly created session with Amazon GameOn
function session_client:authenticate(profile, device_os_type, app_build_type)

    local application = application_provider:get()
    local configuration = configuration_dao.get_default_configuration_for_application(application.id)
    local registration_client = gameon_registration_client:refresh(profile)

    device_os_type = tostring(device_os_type or gameon_constants.device_os_type.html)
    app_build_type = tostring(app_build_type or gameon_constants.app_build_type.release)

    local request = {
        method = "POST",
        base = gameon_constants.USER_BASE_URI,
        path = session_client.PATH,
        headers = {
            [gameon_constants.API_KEY_HEADER] = configuration.publicApiKey
        },
        entity = {
            media_type = "application/json",
            value = {
                playerName = profile.displayName,
                appBuildType = app_build_type,
                deviceOSType = device_os_type,
                playerToken = registration_client.playerToken
            }
        }
    }

    local status, headers, response = http_client.send(request)

    if (status == 200)
    then
        return registration_client:create(response)
    else
        error{ status = status, message = response.message }
    end

end


--- Refreshes a Session
-- Looks up the current session for the player, or if no such session exists, this will authenticate the session so
-- that requests may be made.  If no such seession exists, this will create a new session and store it to the database
-- such that requests may be made to GameOn
--
-- @param profile the profile of the user
-- @param device_os_type the device OS type
-- @param app_build_type the app build type
function session_client:refresh(profile, device_os_type, app_build_type)

    -- Local function which simply performs the registration if necessary

    return util.java.pcallx(
    function()
        local session = gameon_session_dao.get_session_for_profile(profile)
        return session_client:create(session)
    end,
    session_not_found_exception, function(ex)

        local client = session_client:authenticate(profile, device_os_type, app_build_type)
        local session = gameon_session:new()

        session.profile = profile
        session.sessionId = client.id
        session.sessionApiKey = client.sessionApiKey
        session.sessionExpirationDate = client.sessionExpirationDate
        session.deviceOSType = tostring(client.deviceOSType)
        session.appBuildType = tostring(client.appBuildType)

        return client

    end)

end

return session_client
