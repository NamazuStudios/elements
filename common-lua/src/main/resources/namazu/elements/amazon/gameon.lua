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
gameon.session_client = require "namazu.elements.amazon.gameon.session_client"

--- A class for managing registration
gameon.registration_client = require "namazu.elements.amazon.gameon.registration_client"

--- A class for managing GameOn Matches
gameon.match_client = require "namazu.elements.amazon.gameon.match_client"

return gameon
