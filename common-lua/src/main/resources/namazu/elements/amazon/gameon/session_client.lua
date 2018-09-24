--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 7/10/18
-- Time: 9:45 AM
-- To change this template use File | Settings | File Templates.
--

local log = require "namazu.log"
local ioc = require "namazu.ioc.resolver"
local http_client = require "namazu.http.client"
local util = require "namazu.util"


local application_provider = ioc:provider("com.namazustudios.socialengine.model.application.Application")
local configuration_dao = require "namazu.socialengine.dao.application.configuration.gameon"

local gameon_constants = require "namazu.elements.amazon.gameon.constants"
local gameon_session = java.require "com.namazustudios.socialengine.model.gameon.game.GameOnSession"
local gameon_session_dao = require "namazu.elements.dao.gameon.session"
local gameon_registration_client = require "namazu.elements.amazon.gameon.registration_client"
local gameon_match_client = require "namazu.elements.amazon.gameon.match_client"

local device_os_type_e = java.require "com.namazustudios.socialengine.model.gameon.game.DeviceOSType"
local app_build_type_e = java.require "com.namazustudios.socialengine.model.gameon.game.AppBuildType"

local session_not_found_exception = java.require "com.namazustudios.socialengine.exception.gameon.GameOnSessionNotFoundException"

local session_client = {}

--- The base path for session APIs
session_client.PATH = "/players/auth"

--- Raw Constructor
function session_client:new(o)
    o = o or {}
    setmetatable(o, self)
    self.__index = self
    return o
end

--- Creates a new Session
-- This creates a new session with the supplied credentials.  Unlike authenticate, this makes no network calls and is
-- useful for reconstituing a session with existing credentails.
--
-- @param id the session id
-- @param api_key the session api key
-- @param expires the time at which the session expires (stored only for reference, not used in implementation)
function session_client:create(session)
    return session_client:new{
        sessionId = session.sessionId,
        sessionApiKey = session.sessionApiKey,
        sessionExpirationDate = session.sessionExpirationDate
    }
end

--- Authenticates a session (Class Method)
-- Invokes authenticate_with_options using default options.  The only mandatory parameter is the user profile used
-- to make the session.
--
-- @profile the user profile
-- @return a freshly created session with Amazon GameOn
function session_client:authenticate(profile)
    return self:authenticate_with_options{profile = profile}
end

--- Authenticates a session (Class Method)
-- This calls the Amazon APIs to authenticate a new session for a particular user.  Upon successful response, a new
-- session is returned which can be used to manage calls to the Game On APIs.  This call may suspent the currently
-- running coroutine while the request is processed.
--
-- Note, the returned session is not stored in the database.
--
-- @param options the options to use when refreshing
-- @return a freshly created session with Amazon GameOn
function session_client:authenticate_with_options(options)

    local profile = options.profile
    local device_os_type = tostring(options["device_os_type"] or device_os_type_e:getDefault())
    local app_build_type = tostring(options["app_build_type"] or app_build_type_e:getDefault())

    local application = application_provider:get()
    local configuration = configuration_dao.get_default_configuration_for_application(application.id)
    local registration_client = gameon_registration_client:refresh(profile)

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
        return session_client:create(response)
    else
        local message = response and response.message or "<unknown>"
        log.error("HTTP Status {}. Response {}.  Message {}", status, response, message)
        error{ status = status, message = message }
    end

end

--- Refreshes a Session
-- Invokes authenticate_with_options using default options.  The only mandatory parameter is the user profile used
-- to make the session.
--
-- @profile the user profile
-- @return a freshly created session with Amazon GameOn, or one reconstituted from the database
function session_client:refresh(profile)
    return self:refresh_with_options{profile = profile}
end

--- Refreshes a Session
-- Looks up the current session for the player, or if no such session exists, this will authenticate the session so
-- that requests may be made.  If no such seession exists, this will create a new session and store it to the database
-- such that requests may be made to GameOn
--
-- @param options the options to use when refreshing
-- @return a freshly created session with Amazon GameOn, or one reconstituted from the database
function session_client:refresh_with_options(options)

    local profile = options.profile
    local device_os_type = tostring(options["device_os_type"] or device_os_type_e:getDefault())
    local app_build_type = tostring(options["app_build_type"] or app_build_type_e:getDefault())

    return util.java.pcallx(
    function()
        local session = gameon_session_dao.get_session_for_profile(profile, device_os_type)
        return session_client:create(session)
    end,
    session_not_found_exception, function(ex)

        local client = session_client:authenticate_with_options(options)

        local session = gameon_session:new()
        session.profile = profile
        session.sessionId = client.sessionId
        session.sessionApiKey = client.sessionApiKey
        session.sessionExpirationDate = client.sessionExpirationDate
        session.deviceOSType = device_os_type_e:valueOf(tostring(device_os_type))
        session.appBuildType = app_build_type_e:valueOf(tostring(app_build_type))
        gameon_session_dao.create_session(session)

        return client

    end)

end

--- Returns a Match Client
-- Returns the match client for the supplied match_id.
--
-- @param match_id the match to submit the
function session_client:match_client(match_id)
    return gameon_match_client:new{
        matchId = match_id,
        sessionId = self.sessionId,
        sessionApiKey = self.sessionApiKey
    }
end

return session_client
