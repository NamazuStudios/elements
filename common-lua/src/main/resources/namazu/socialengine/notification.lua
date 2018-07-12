--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 3/29/18
-- Time: 1:59 PM
--

-- Moved to namazu.elements.auth

local log = require "namazu.log"
local notification = require "namazu.elements.auth"
log.warn("namazu.socialengine.notification is deprecated.  Use namazu.elements.notification instead.")
return notification
