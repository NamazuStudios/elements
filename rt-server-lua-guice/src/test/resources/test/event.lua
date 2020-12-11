--
-- Created by IntelliJ IDEA.
-- User: garrettmcspadden
-- Date: 12/10/20
-- Time: 3:45 PM
-- To change this template use File | Settings | File Templates.
--

local util = require "namazu.util"
local test_java_event = require "test.java.event"

local event = {}

function event.hello_event()
    print("hello test event from inside lua!")
    test_java_event.hello_world_event()
end

function event.hello_event_again()
    print("hello test event again from inside lua!")
    test_java_event.hello_world_event()
end

return event

