--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 3/29/18
-- Time: 1:59 PM
--

local ioc = require "namazu.ioc.resolver"

local Application = java.require "com.namazustudios.socialengine.model.application.Application"
local application_builder_provider = ioc:provider("com.namazustudios.socialengine.service.NotificationBuilder")
local application_provider = ioc:provider("com.namazustudios.socialengine.model.application.Application")

local notification = {}

--- Creates a NotificationBuilder
-- This creates an instance of NotificationBuilder which can be used to create and send Notifications.  If available,
-- this will query the current Attributes to see if an instance of Application is availble, and it will pre-configure
-- the instance with the application.
--
-- Note that the returned NotificationBuilder will allow for reconfiguring any desired attribute.
--
-- @return a new instance of NotificationBuilder
function notification.builder()
    local builder = application_builder_provider:get()
    local application = application_provider:get()
    return builder:application(application)
end

return notification
