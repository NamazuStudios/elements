--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 11/19/17
-- Time: 10:22 AM
-- To change this template use File | Settings | File Templates.
--

-- Moved to namazu.elements.auth
local log = require "namazu.log"
local auth = require "namazu.elements.auth"
log.warn("namazu.socialengine.auth is deprecated.  Use namazu.elements.auth instead.")
return auth
