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

local registration = {}

--- Raw Constructor for Registration
-- This allocates a new instance, sets the metatable,
function registration:new(registration)
    registration = registration or {}
    registration.__index = registration
    setmetatable(registration, self)
    return registration
end

function registration:refresh(profile)
end

return registration
