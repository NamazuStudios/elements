--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/19/17
-- Time: 10:22 AM
-- To change this template use File | Settings | File Templates.
--

-- Moved to eci.elements.auth
local log = require "eci.log"
local auth = require "namazu.elements.auth"
log.warn("namazu.elements.auth is deprecated.  Use eci.elements.auth instead.")
return auth
