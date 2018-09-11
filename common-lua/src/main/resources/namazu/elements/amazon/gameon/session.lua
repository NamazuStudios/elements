--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 7/10/18
-- Time: 9:45 AM
-- To change this template use File | Settings | File Templates.
--

local http_client = require "namazu.http.client"
local ioc = require "namazu.ioc.resolver"
local application_provider = ioc:provider("com.namazustudios.socialengine.model.application.Application")
local configuration_dao = require "namazu.socialengine.dao.application.configuration.gameon"

local gameon_constants = require "namazu.elements.amazon.gameon.constants"
local gameon_session = java.require "com.namazustudios.socialengine.model.gameon.game.GameOnSession"
local gameon_session_dao = require "namazu.elements.dao.gameon.session"

local session_not_found_exception = java.require "com.namazustudios.socialengine.exception.gameon.GameOnSessionNotFoundException"

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
function session.create(id, api_key, expires)
    return session:new{id = id, api_key = api_key, expires = expires}
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
function session.authenticate(profile, device_os_type, app_build_type)

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


--- Refreshes a Session
-- Looks up the current session for the player, or if no such session exists, this will authenticate the session so
-- that requests may be made.  If no such seession exists, this will create a new session and store it to the database
-- such that requests may be made to GameOn
--
-- @param profile the profile of the user
-- @param device_os_type the device OS type
-- @param app_build_type the app build type
function session.refresh(profile, device_os_type, app_build_type)

    -- Local function which simply performs the registration if necessary

    local function register_if_necessary()
        return java.pcallx(
            function()
                return gameon_registration_dao:get_registration_for_profile(profile)
            end,
            registration_not_found_exception, function(ex)
                -- TODO Return registration from Amazon API
                return nil;
            end
        )
    end

    return util.java.pcallx(
        function()

            local go_session = gameon_session_dao.get_session_for_profile(profile)

            return session:create(
                go_session.session_id,
                go_session.session_api_key,
                go_session.session_expiration_date
            )

        end,
        session_not_found_exception, function(ex)

            local session = session:authenticate(profile, device_os_type, app_build_type)
            local go_session = gameon_session:new()
            go_session.profile = profile
            go_session.session_id = session.id
            go_session.session_api_key = session.api_key
            go_session.session_expiration_date = session.expires
            gameon_session_dao.create_session(go_session)

            return session

        end
    )

end

return session
