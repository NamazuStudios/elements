--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/19/17
-- Time: 10:22 AM
-- To change this template use File | Settings | File Templates.
--

-- Moved to eci.elements.mongodb

local log = require "eci.log"
local notification = require "eci.elements.notification"
log.warn("namazu.elements.notification is deprecated.  Use eci.elements.notification instead.")
return notification
