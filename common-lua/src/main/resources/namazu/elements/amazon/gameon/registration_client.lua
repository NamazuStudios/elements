--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 9/11/18
-- Time: 3:54 PM
-- To change this template use File | Settings | File Templates.
--

local http_client = require "namazu.http.client"
local ioc = require "namazu.ioc.resolver"

local application_provider = ioc:provider("com.namazustudios.socialengine.model.application.Application")
local configuration_dao = require "namazu.socialengine.dao.application.configuration.gameon"

local gameon_constants = require "namazu.elements.amazon.gameon.constants"
local gameon_registration = java.require "com.namazustudios.socialengine.model.gameon.game.GameOnRegistration"
local gameon_registration_dao = require "namazu.elements.dao.gameon.registration"

local registration_not_found_exception = java.require "com.namazustudios.socialengine.exception.gameon.GameOnRegistrationNotFoundException"

local registration_client = {}

--- The API Path for the Registration Endpoint
registration_client.PATH = "/players/registration"

--- Raw Constructor for Registration
-- This allocates a new instance, sets the metatable, and returns the instance created.
function registration_client:new(registration_client)
    registration_client = {}
    registration_client.__index = registration
    setmetatable(registration_client, self)
    return registration_client
end

--- Creates an Instance of Registration
-- Provided a table (or table-like object) of registration, this creates an instance of registration from the supplied
-- properties.
--
-- @param registration the registration model object
function registration_client:create(registration)
    return registration_client:new{
        id = registration.id,
        profile = registration.profile,
        playerToken = registration.playerToken,
        externalPlayerId = registration.externalPlayerId
    }
end

--- Registers the supplied Profile with GameOn
-- Invokes the necessary APIs to create the instance of the registration client.
--
-- @param registration the registration model object
-- @returns the client instance or raises an error indicating that the call failed
function registration_client:register()

    -- Gets the current Application and Aplication Configuration
    local application = application_provider:get()
    local configuration = configuration_dao.get_default_configuration_for_application(application.id)

    local request = {
        method = "POST",
        base = gameon_constants.USER_BASE_URI,
        path = registration_client.PATH,
        headers = {
            [gameon_constants.API_KEY_HEADER] = configuration.publicApiKey
        },
        entity = {
            media_type = "application/json",
            value = nil
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

--- Refreshes Registration
-- Supplying a profile, this will create a new registration if none such exists for the supplied profile.  If the
-- registration exists, then this will return a client instance which can be used to establish sessions.
--
-- @param profile the profile to associate with the registration
-- @return the registration client
function registration_client:refresh(profile)
    return util.java.pcallx(
    function()
        local registration = gameon_registration_dao.get_registration_for_profile(profile)
        return registration_client:create(registration)
    end,
    registration_not_found_exception, function (ex)

        local client = registration_client:register()

        -- Set properties in the model and save to the database so it can be referenced later and cross-linked to
        -- the supplied profile.

        local registration = gameon_registration:new()
        registration.profile = profile
        registration.playerToken = client.playerToken
        registration.externalPlayerId = client.externalPlayerId
        gameon_registration_dao.create_registration(registration)

        return client

    end)
end

return registration_client
