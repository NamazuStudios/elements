--
-- Created by IntelliJ IDEA.
-- User: patricktwohig
-- Date: 3/30/18
-- Time: 4:54 PM
-- To change this template use File | Settings | File Templates.
--

local test_notification = {}

local auth = require "eci.elements.auth"
local notification = require "eci.elements.notification"

function test_notification.test_send_with_builder()

    local builder = notification.builder()

    builder:title("Hello World!")
           :message("Hello World!")
           :recipient(auth.profile())
           :add("single","property")
           :addAll({extraA ="foo", extraB = "bar"})
           :build()
           :send()

end

return test_notification
