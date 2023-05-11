--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/19/17
-- Time: 10:22 AM
-- To change this template use File | Settings | File Templates.
--

-- Moved to eci.elements.mongodb
local log = require "eci.log"
local mongodb = require "eci.elements.mongodb"
log.warn("namazu.elements.mongodb is deprecated.  Use eci.elements.mongodb instead.")
return mongodb
