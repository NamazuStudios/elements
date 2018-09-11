--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 7/10/18
-- Time: 9:27 AM
-- To change this template use File | Settings | File Templates.
--

local gameon = {}

--- Amazon GameOn related constants
gameon.constants = require "namazu.elements.amazon.gameon.constants"

--- A class for managing session
gameon.session = require "namazu.elements.amazon.gameon.session"

--- A class for managing registration
gameon.session = require "namazu.elements.amazon.gameon.registration"

return gameon
